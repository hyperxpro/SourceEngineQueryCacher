package com.aayushatharva.sourcecenginequerycacher;

import com.aayushatharva.sourcecenginequerycacher.backenddispatchers.A2S_Info;
import com.aayushatharva.sourcecenginequerycacher.backenddispatchers.A2S_Player;
import com.aayushatharva.sourcecenginequerycacher.utils.CacheCleaner;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

public class Main {

    public static InetAddress CacherIP;
    public static int CacherPort;
    public static InetAddress GameServerIP;
    public static int GameServerPort;

    static {
        try {
            Properties Data = new Properties();
            Data.load(new FileInputStream("Cacher.conf"));

            CacherIP = InetAddress.getByName((String) Data.get("CacherIP"));
            CacherPort = Integer.parseInt((String) Data.get("CacherPort"));
            GameServerIP = InetAddress.getByName((String) Data.get("GameServerIP"));
            GameServerPort = Integer.parseInt((String) Data.get("GameServerPort"));

            Data.clear(); // Clear Properties
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {

        EventLoopGroup eventLoopGroup;

        // Use Epoll when available
        if (Epoll.isAvailable()) {
            eventLoopGroup = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        } else {
            eventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        }

        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .channelFactory(() -> {
                    if (Epoll.isAvailable()) {
                        EpollDatagramChannel epollDatagramChannel = new EpollDatagramChannel();

                        epollDatagramChannel.config().setReceiveBufferSize(65535);
                        epollDatagramChannel.config().setSendBufferSize(65535);
                        epollDatagramChannel.config().setAllocator(PooledByteBufAllocator.DEFAULT);
                        epollDatagramChannel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(65535));

                        return epollDatagramChannel;
                    } else {
                        NioDatagramChannel nioDatagramChannel = new NioDatagramChannel();

                        nioDatagramChannel.config().setReceiveBufferSize(65535);
                        nioDatagramChannel.config().setSendBufferSize(65535);
                        nioDatagramChannel.config().setAllocator(PooledByteBufAllocator.DEFAULT);
                        nioDatagramChannel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(65535));

                        return nioDatagramChannel;
                    }
                })
                .handler(new Handler());

        // Bind and Start Server
        ChannelFuture channelFuture = bootstrap.bind(CacherIP, CacherPort);

        System.out.println("Server Started on Socket: " + CacherIP.getHostAddress() + ":" + CacherPort);

        new A2S_Info().start();
        new A2S_Player().start();
        new Stats().start();
        new CacheCleaner().start();

        // Keep Running
        channelFuture.syncUninterruptibly();
    }
}
