package com.aayushatharva.sourcecenginequerycacher.gameserver.a2splayer;

import com.aayushatharva.sourcecenginequerycacher.Main;
import com.aayushatharva.sourcecenginequerycacher.utils.Config;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollChannelOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PlayerClient extends Thread {

    private static final Logger logger = LogManager.getLogger(PlayerClient.class);
    private boolean keepRunning = true;

    public PlayerClient(String name) {
        super(name);
    }

    @SuppressWarnings("BusyWait")
    public void run() {
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(Main.eventLoopGroup)
                    .channelFactory(EpollDatagramChannel::new)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_SNDBUF, Config.SendBufferSize)
                    .option(ChannelOption.SO_RCVBUF, Config.ReceiveBufferSize)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(Config.ReceiveAllocatorBufferSizeMin,
                            Config.ReceiveAllocatorBufferSize, Config.ReceiveAllocatorBufferSizeMax))
                    .handler(new PlayerHandler());

            Channel channel = bootstrap.connect(Config.GameServer).sync().channel();

            while (keepRunning) {
                channel.writeAndFlush(Packets.A2S_PLAYER_CHALLENGE_REQUEST_2).sync();
                try{
                    sleep(Config.GameUpdateInterval);
                } catch(InterruptedException e){
                    logger.error("Error at PlayerClient During Interval Sleep ", e);
                    break;
                }
            }

            channel.closeFuture().sync();
        } catch (Exception ex) {
            logger.atError().withThrowable(ex).log("Error occurred");
        }
    }

    public void shutdown() {
        this.keepRunning = false;
        this.interrupt();
    }
}
