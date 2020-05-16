package com.aayushatharva.sourcecenginequerycacher;

import com.aayushatharva.sourcecenginequerycacher.gameserver.a2sinfo.InfoClient;
import com.aayushatharva.sourcecenginequerycacher.gameserver.a2splayer.PlayerClient;
import com.aayushatharva.sourcecenginequerycacher.utils.CacheCleaner;
import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import com.aayushatharva.sourcecenginequerycacher.utils.Config;
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

    public static final ByteBufAllocator BYTE_BUF_ALLOCATOR = PooledByteBufAllocator.DEFAULT;
    private static final Logger logger = LogManager.getLogger(Main.class);

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
                    // Epoll is selected but Epoll is not available then throw error.
                    throw new IllegalArgumentException("Epoll Transport is not available");
                }
            } else if (Config.Transport.equalsIgnoreCase("nio")) {
                eventLoopGroup = new NioEventLoopGroup(Config.Threads);
            } else {
                throw new IllegalArgumentException("Invalid Transport Type: " + Config.Transport);
            }

            Bootstrap bootstrap = new Bootstrap()
                    .group(eventLoopGroup)
                    .channelFactory(() -> {
                        if (Config.Transport.equalsIgnoreCase("epoll") && Epoll.isAvailable()) {
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

        if (CacheHub.A2S_INFO.get() != null && CacheHub.A2S_INFO.get().refCnt() > 0) {
            CacheHub.A2S_INFO.get().release();
        }

        if (CacheHub.A2S_PLAYER.get() != null && CacheHub.A2S_PLAYER.get().refCnt() > 0) {
            CacheHub.A2S_PLAYER.get().release();
        }

        stats.shutdown();
        cacheCleaner.shutdown();
        future.get();

        // Call GC to wipe out everything
        System.gc();
    }
}
