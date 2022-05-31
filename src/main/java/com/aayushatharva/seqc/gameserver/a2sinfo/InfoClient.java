package com.aayushatharva.seqc.gameserver.a2sinfo;

import com.aayushatharva.seqc.utils.Configuration;
import com.aayushatharva.seqc.utils.Packets;
import io.netty5.bootstrap.Bootstrap;
import io.netty5.buffer.api.BufferAllocator;
import io.netty5.channel.Channel;
import io.netty5.channel.ChannelOption;
import io.netty5.channel.EventLoopGroup;
import io.netty5.channel.MultithreadEventLoopGroup;
import io.netty5.channel.nio.NioHandler;
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
                    .handler(new InfoHandler());

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
