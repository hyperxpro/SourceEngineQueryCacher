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
package com.aayushatharva.seqc;

import com.aayushatharva.seqc.gameserver.a2sinfo.InfoClient;
import com.aayushatharva.seqc.gameserver.a2splayer.PlayerClient;
import com.aayushatharva.seqc.gameserver.a2srules.RulesClient;
import com.aayushatharva.seqc.utils.Cache;
import com.aayushatharva.seqc.utils.Configuration;
import io.netty5.bootstrap.Bootstrap;
import io.netty5.buffer.api.BufferAllocator;
import io.netty5.channel.Channel;
import io.netty5.channel.ChannelOption;
import io.netty5.channel.EventLoopGroup;
import io.netty5.channel.FixedRecvBufferAllocator;
import io.netty5.channel.IoHandlerFactory;
import io.netty5.channel.MultithreadEventLoopGroup;
import io.netty5.channel.epoll.Epoll;
import io.netty5.channel.epoll.EpollDatagramChannel;
import io.netty5.channel.epoll.EpollHandler;
import io.netty5.channel.nio.NioHandler;
import io.netty5.channel.socket.InternetProtocolFamily;
import io.netty5.channel.socket.nio.NioDatagramChannel;
import io.netty5.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public final class Main {
    static {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static EventLoopGroup eventLoopGroup;
    private static Stats stats;
    private static InfoClient infoClient;
    private static PlayerClient playerClient;
    private static RulesClient rulesClient;

    public static void main(String[] args) {
        try {
            // Setup configurations
            Configuration.setup(args[0]);

            IoHandlerFactory ioHandlerFactory;
            if (Configuration.TRANSPORT.equalsIgnoreCase("epoll")) {
                if (Epoll.isAvailable()) {
                    logger.info("Using Epoll Transport");
                    ioHandlerFactory = EpollHandler.newFactory();
                } else {
                    logger.error("Epoll Transport is not available");
                    System.exit(1);
                    return;
                }
            } else if (Configuration.TRANSPORT.equalsIgnoreCase("nio")) {
                logger.info("Using Nio Transport");
                ioHandlerFactory = NioHandler.newFactory();
            } else {
                logger.error("Unknown Transport");
                System.exit(1);
                return;
            }

            eventLoopGroup = new MultithreadEventLoopGroup(Configuration.THREADS, ioHandlerFactory);

            List<Future<Channel>> channelFutures = new ArrayList<>();

            Bootstrap bootstrap = new Bootstrap()
                    .group(eventLoopGroup)
                    .channelFactory(eventLoop -> {
                        if (Epoll.isAvailable()) {
                            EpollDatagramChannel epollDatagramChannel = new EpollDatagramChannel(eventLoop, InternetProtocolFamily.IPv4);
                            epollDatagramChannel.config().setUdpGro(true);
                            epollDatagramChannel.config().setReusePort(true);
                            return epollDatagramChannel;
                        } else {
                            return new NioDatagramChannel(eventLoop, InternetProtocolFamily.IPv4);
                        }
                    })
                    .option(ChannelOption.SO_SNDBUF, Configuration.SEND_BUFFER_SIZE)
                    .option(ChannelOption.SO_RCVBUF, Configuration.RECEIVE_BUFFER_SIZE)
                    .option(ChannelOption.BUFFER_ALLOCATOR, BufferAllocator.offHeapPooled())
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvBufferAllocator(Configuration.RECEIVE_ALLOCATOR_SIZE).respectMaybeMoreData(false))
                    .handler(Handler.INSTANCE);

            int bindRounds = 1;
            if (Configuration.TRANSPORT.equalsIgnoreCase("epoll")) {
                bindRounds = Configuration.THREADS;
            }

            for (int i = 0; i < bindRounds; i++) {
                // Bind and Start Server
                Future<Channel> channelFuture = bootstrap.bind(Configuration.SERVER_ADDRESS.getAddress(), Configuration.SERVER_ADDRESS.getPort())
                        .addListener(future -> {
                            if (future.isSuccess()) {
                                logger.info("Server Started on Address: {}:{}",
                                        ((InetSocketAddress) future.get().localAddress()).getAddress().getHostAddress(),
                                        ((InetSocketAddress) future.get().localAddress()).getPort());
                            } else {
                                logger.error("Caught Error While Starting Server", future.cause());
                                System.exit(1);
                            }
                        });

                channelFutures.add(channelFuture);
            }

            // Wait for all bind sockets to start
            for (Future<Channel> channelFuture : channelFutures) {
                channelFuture.sync();
            }

            if (Configuration.STATS_BPS || Configuration.STATS_PPS) {
                stats = new Stats();
                stats.start();
            }

            infoClient = new InfoClient("A2SInfoClient");
            playerClient = new PlayerClient("A2SPlayerClient");
            rulesClient = new RulesClient("A2SRulesClient");

            infoClient.start();

            if (Configuration.ENABLE_A2S_PLAYER) {
                playerClient.start();
            }

            if (Configuration.ENABLE_A2S_RULE) {
                rulesClient.start();
            }
        } catch (Exception ex) {
            logger.atError().withThrowable(ex).log("Error while Initializing");
        }
    }

    /**
     * Shutdown everything
     */
    public static void shutdown() throws ExecutionException, InterruptedException {
        Future<?> future = eventLoopGroup.shutdownGracefully();
        infoClient.shutdown();
        playerClient.shutdown();
        rulesClient.shutdown();
        Cache.CHALLENGE_MAP.clear();

        if (Configuration.STATS_BPS || Configuration.STATS_PPS) {
            stats.shutdown();
        }

        future.get();
    }
}
