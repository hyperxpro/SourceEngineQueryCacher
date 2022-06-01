/*
 * This file is part of SourceEngineQueryCacher. [https://github.com/hyperxpro/SourceEngineQueryCacher]
 * Copyright (c) 2020-2022 Aayush Atharva
 *
 * SourceEngineQueryCacher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SourceEngineQueryCacher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SourceEngineQueryCacher.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.aayushatharva.seqc.gameserver.a2sinfo;

import com.aayushatharva.seqc.gameserver.SplitPacketDecoder;
import com.aayushatharva.seqc.utils.Configuration;
import com.aayushatharva.seqc.utils.Packets;
import io.netty5.bootstrap.Bootstrap;
import io.netty5.buffer.api.BufferAllocator;
import io.netty5.channel.Channel;
import io.netty5.channel.ChannelInitializer;
import io.netty5.channel.ChannelOption;
import io.netty5.channel.EventLoopGroup;
import io.netty5.channel.MultithreadEventLoopGroup;
import io.netty5.channel.nio.NioHandler;
import io.netty5.channel.socket.DatagramChannel;
import io.netty5.channel.socket.nio.NioDatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;

public final class InfoClient extends Thread {

    private static final Logger logger = LogManager.getLogger(InfoClient.class);
    private static final EventLoopGroup EVENT_LOOP = new MultithreadEventLoopGroup(NioHandler.newFactory());
    private boolean keepRunning = true;

    public InfoClient(String name) {
        super(name);
    }

    @SuppressWarnings("BusyWait")
    public void run() {
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(EVENT_LOOP)
                    .channelFactory(NioDatagramChannel::new)
                    .option(ChannelOption.BUFFER_ALLOCATOR, BufferAllocator.offHeapPooled())
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) {
                            ch.pipeline().addLast(new SplitPacketDecoder());
                            ch.pipeline().addLast(new InfoHandler());
                        }
                    });

            Channel channel = bootstrap.connect(Configuration.GAME_ADDRESS).sync().get();

            while (keepRunning) {
                channel.writeAndFlush(Packets.A2S_INFO_REQUEST.copy()).sync();
                try {
                    sleep(Configuration.UPDATE_RATE);
                } catch (InterruptedException e) {
                    logger.error("Sleep Interrupted");
                    break;
                }
            }

            channel.close().sync();
        } catch (Exception ex) {
            logger.atError().withThrowable(ex).log("Error occurred");
        }
    }

    public void shutdown() throws ExecutionException, InterruptedException {
        this.keepRunning = false;
        EVENT_LOOP.shutdownGracefully().get();
        this.interrupt();
    }
}
