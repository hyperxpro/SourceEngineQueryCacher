package com.aayushatharva.seqc;

import com.aayushatharva.seqc.gameserver.a2sinfo.InfoClient;
import com.aayushatharva.seqc.gameserver.a2splayer.PlayerClient;
import com.aayushatharva.seqc.gameserver.a2srules.RulesClient;
import com.aayushatharva.seqc.utils.Cache;
import com.aayushatharva.seqc.utils.Config;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.unix.UnixChannelOption;
import io.netty.incubator.channel.uring.IOUring;
import io.netty.incubator.channel.uring.IOUringDatagramChannel;
import io.netty.incubator.channel.uring.IOUringEventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    static {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
    }

    public static EventLoopGroup eventLoopGroup;
    private static Stats stats;
    private static InfoClient infoClient;
    private static PlayerClient playerClient;
    private static RulesClient rulesClient;

    public static void main(String[] args) {

        try {

            // Setup configurations
            Config.setup(args);

            if (IOUring.isAvailable()) {
                eventLoopGroup = new IOUringEventLoopGroup(Config.Threads);
                logger.info("Using High Performance IO_URING Transport");
            } else {
                logger.info("High Performance IO_URING Transport is not available");

                if (Epoll.isAvailable()) {
                    eventLoopGroup = new EpollEventLoopGroup(Config.Threads);
                } else {
                    // Epoll is requested but Epoll is not available, we'll throw error and shut down.
                    logger.fatal("IO_URING and Epoll Transport, both are not available. Shutting down...", Epoll.unavailabilityCause());
                    System.exit(1);
                }
            }

            List<ChannelFuture> channelFutureList = new ArrayList<>();
            Handler handler = new Handler();

            Bootstrap bootstrap = new Bootstrap()
                    .group(eventLoopGroup)
                    .channelFactory(() -> {
                        if (IOUring.isAvailable()) {
                            return new IOUringDatagramChannel(InternetProtocolFamily.IPv4);
                        } else {
                            return new EpollDatagramChannel(InternetProtocolFamily.IPv4);
                        }
                    })
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_SNDBUF, Config.SendBufferSize)
                    .option(ChannelOption.SO_RCVBUF, Config.ReceiveBufferSize)
                    .option(EpollChannelOption.MAX_DATAGRAM_PAYLOAD_SIZE, Config.ReceiveAllocatorBufferSize)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(Config.ReceiveAllocatorBufferSize))
                    .option(UnixChannelOption.SO_REUSEPORT, true)
                    .option(EpollChannelOption.UDP_GRO, true)
                    .handler(handler);

            for (int i = 0; i < Config.Threads; i++) {
                // Bind and Start Server
                ChannelFuture channelFuture = bootstrap.bind(Config.ServerAddress.getAddress(), Config.ServerAddress.getPort())
                        .addListener((ChannelFutureListener) future -> {
                            if (future.isSuccess()) {
                                logger.info("Server Started on Address: {}:{}",
                                        ((InetSocketAddress) future.channel().localAddress()).getAddress().getHostAddress(),
                                        ((InetSocketAddress) future.channel().localAddress()).getPort());
                            } else {
                                logger.error("Caught Error While Starting Server", future.cause());
                                System.err.println("Shutting down...");
                                System.exit(1);
                            }
                        });

                channelFutureList.add(channelFuture);
            }

            // Wait for all bind sockets to start
            for (ChannelFuture channelFuture : channelFutureList) {
                channelFuture.sync();
            }

            if (Config.Stats_BPS || Config.Stats_PPS) {
                stats = new Stats();
                stats.start();
            }

            infoClient = new InfoClient("A2SInfoClient");
            playerClient = new PlayerClient("A2SPlayerClient");
            rulesClient = new RulesClient("A2SRulesClient");

            infoClient.start();
            playerClient.start();

            if (Config.EnableA2SRule)
                rulesClient.start();
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

        if (Config.Stats_BPS || Config.Stats_PPS)
            stats.shutdown();

        future.get();
    }
}
