package com.aayushatharva.sourcecenginequerycacher;

import com.aayushatharva.sourcecenginequerycacher.gameserver.a2sinfo.InfoClient;
import com.aayushatharva.sourcecenginequerycacher.gameserver.a2splayer.PlayerClient;
import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import com.aayushatharva.sourcecenginequerycacher.utils.Config;
import com.aayushatharva.sourcecenginequerycacher.utils.Utils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.unix.UnixChannelOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static final ByteBufAllocator BYTE_BUF_ALLOCATOR = PooledByteBufAllocator.DEFAULT;
    public static EventLoopGroup eventLoopGroup;
    private static Stats stats;
    private static InfoClient infoClient;
    private static PlayerClient playerClient;

    public static void main(String[] args) {

        try {

            // Setup configurations
            Config.setup(args);

            // Use Epoll when available
            if (Epoll.isAvailable()) {
                eventLoopGroup = new EpollEventLoopGroup(Config.Threads);
            } else {
                // Epoll is requested but Epoll is not available so we'll throw error and shut down.
                System.err.println("Epoll Transport is not available, shutting down...");
                System.exit(1);
            }

            List<ChannelFuture> channelFutureList = new ArrayList<>();
            Handler handler = new Handler();

            Bootstrap bootstrap = new Bootstrap()
                    .group(eventLoopGroup)
                    .channelFactory(() -> new EpollDatagramChannel(InternetProtocolFamily.IPv4))
                    .option(ChannelOption.ALLOCATOR, BYTE_BUF_ALLOCATOR)
                    .option(ChannelOption.SO_SNDBUF, Config.SendBufferSize)
                    .option(ChannelOption.SO_RCVBUF, Config.ReceiveBufferSize)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
                    .option(UnixChannelOption.SO_REUSEPORT, true)
                    .option(EpollChannelOption.UDP_GRO, true) // Enable UDP GRO
                    .handler(handler);

            for (int i = 0; i < Config.Threads; i++) {
                // Bind and Start Server
                ChannelFuture channelFuture = bootstrap.bind(Config.LocalServer.getAddress(), Config.LocalServer.getPort()).addListener((ChannelFutureListener) future -> {
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

            stats = new Stats();
            infoClient = new InfoClient("A2SInfoClient");
            playerClient = new PlayerClient("A2SPlayerClient");

            stats.start();
            infoClient.start();
            playerClient.start();
        } catch (Exception ex) {
            logger.atError().withThrowable(ex).log("Error while Initializing");
        }
    }

    /**
     * Shutdown everything
     */
    public void shutdown() throws ExecutionException, InterruptedException {
        Future<?> future = eventLoopGroup.shutdownGracefully();
        infoClient.shutdown();
        playerClient.shutdown();
        CacheHub.CHALLENGE_MAP.clear();

        Utils.safeRelease(CacheHub.A2S_INFO);
        Utils.safeRelease(CacheHub.A2S_PLAYER);

        stats.shutdown();
        future.get();
    }
}
