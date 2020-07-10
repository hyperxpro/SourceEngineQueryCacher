package com.aayushatharva.sourcecenginequerycacher;

import com.aayushatharva.sourcecenginequerycacher.gameserver.a2sinfo.InfoClient;
import com.aayushatharva.sourcecenginequerycacher.gameserver.a2splayer.PlayerClient;
import com.aayushatharva.sourcecenginequerycacher.utils.CacheCleaner;
import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import com.aayushatharva.sourcecenginequerycacher.utils.Config;
import com.aayushatharva.sourcecenginequerycacher.utils.Utils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static final ByteBufAllocator BYTE_BUF_ALLOCATOR = PooledByteBufAllocator.DEFAULT;
    public static EventLoopGroup eventLoopGroup;
    private static Stats stats;
    private static CacheCleaner cacheCleaner;
    private static InfoClient infoClient;
    private static PlayerClient playerClient;

    public static void main(String[] args) {

        try {

            // Setup configurations
            Config.setup(args);

            // Use Epoll when available
            if (Config.Transport.equalsIgnoreCase("epoll")) {
                if (Epoll.isAvailable()) {
                    eventLoopGroup = new EpollEventLoopGroup(Config.Threads);
                } else {
                    // Epoll is requested but Epoll is not available so we'll throw error and shut down.
                    System.err.println("Epoll Transport is not available, shutting down...");
                    System.exit(1);
                }
            } else if (Config.Transport.equalsIgnoreCase("nio")) {
                eventLoopGroup = new NioEventLoopGroup(Config.Threads);
            } else {
                System.err.println("Invalid Transport Type: " + Config.Transport + ", shutting down...");
                System.exit(1);
            }

            Bootstrap bootstrap = new Bootstrap()
                    .group(eventLoopGroup)
                    .channelFactory(() -> {
                        if (Config.Transport.equalsIgnoreCase("epoll")) {
                            return new EpollDatagramChannel(InternetProtocolFamily.IPv4);
                        } else {
                            return new NioDatagramChannel(InternetProtocolFamily.IPv4);
                        }
                    })
                    .option(ChannelOption.ALLOCATOR, BYTE_BUF_ALLOCATOR)
                    .option(ChannelOption.SO_SNDBUF, Config.SendBufferSize)
                    .option(ChannelOption.SO_RCVBUF, Config.ReceiveBufferSize)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(Config.FixedReceiveAllocatorBufferSize))
                    .handler(new Handler());

            // Bind and Start Server
            ChannelFuture channelFuture = bootstrap.bind(Config.LocalServer.getAddress(), Config.LocalServer.getPort()).sync();

            logger.atInfo().log("Server Started on Address: {}:{}",
                    ((InetSocketAddress) channelFuture.channel().localAddress()).getAddress().getHostAddress(),
                    ((InetSocketAddress) channelFuture.channel().localAddress()).getPort());

            stats = new Stats();
            cacheCleaner = new CacheCleaner();
            infoClient = new InfoClient("A2SInfoClient");
            playerClient = new PlayerClient("A2SPlayerClient");

            stats.start();
            cacheCleaner.start();
            infoClient.start();
            playerClient.start();

            // Keep Running
            channelFuture.syncUninterruptibly();
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
        CacheHub.CHALLENGE_CACHE.invalidateAll();
        CacheHub.CHALLENGE_CACHE.cleanUp();

        Utils.safeRelease(CacheHub.A2S_INFO.get());
        Utils.safeRelease(CacheHub.A2S_PLAYER.get());

        stats.shutdown();
        cacheCleaner.shutdown();
        future.get();

        // Call GC to wipe out everything
        System.gc();
    }
}
