package com.aayushatharva.sourcecenginequerycacher;

import com.aayushatharva.sourcecenginequerycacher.gameserver.A2SINFO_Worker;
import com.aayushatharva.sourcecenginequerycacher.gameserver.A2SPLAYER_Worker;
import com.aayushatharva.sourcecenginequerycacher.utils.CacheCleaner;
import com.aayushatharva.sourcecenginequerycacher.utils.Config;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        try {

            // Setup configurations
            Config.setup(args);

            // EventLoopGroup
            EventLoopGroup eventLoopGroup;

            // Use Epoll when available
            if (Config.Transport.equalsIgnoreCase("epoll")) {
                if (Epoll.isAvailable()) {
                    eventLoopGroup = new EpollEventLoopGroup(Config.Threads);
                } else {
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
                            EpollDatagramChannel epollDatagramChannel = new EpollDatagramChannel();

                            epollDatagramChannel.config().setReceiveBufferSize(Config.ReceiveBufferSize);
                            epollDatagramChannel.config().setSendBufferSize(Config.SendBufferSize);
                            epollDatagramChannel.config().setAllocator(PooledByteBufAllocator.DEFAULT);
                            epollDatagramChannel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(Config.FixedReceiveAllocatorBufferSize));

                            return epollDatagramChannel;
                        } else {
                            NioDatagramChannel nioDatagramChannel = new NioDatagramChannel();

                            nioDatagramChannel.config().setReceiveBufferSize(Config.ReceiveBufferSize);
                            nioDatagramChannel.config().setSendBufferSize(Config.SendBufferSize);
                            nioDatagramChannel.config().setAllocator(PooledByteBufAllocator.DEFAULT);
                            nioDatagramChannel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(Config.FixedReceiveAllocatorBufferSize));

                            return nioDatagramChannel;
                        }
                    })
                    .handler(new Handler());

            // Bind and Start Server
            ChannelFuture channelFuture = bootstrap.bind(Config.IPAddress, Config.Port).await();

            logger.atInfo().log("Server Started on Address: " + ((InetSocketAddress) channelFuture.channel().localAddress()).getAddress().getHostAddress() + ":" + ((InetSocketAddress) channelFuture.channel().localAddress()).getPort());

            new A2SINFO_Worker("A2S_INFO").start();
            new A2SPLAYER_Worker("A2S_PLAYER").start();
            new Stats().start();
            new CacheCleaner().start();

            // Keep Running
            channelFuture.syncUninterruptibly();
        } catch (Exception ex) {
            logger.atError().withThrowable(ex).log("Error while Initializing");
        }
    }
}
