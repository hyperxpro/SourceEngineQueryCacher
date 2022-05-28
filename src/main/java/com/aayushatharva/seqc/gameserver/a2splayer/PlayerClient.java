package com.aayushatharva.seqc.gameserver.a2splayer;

import com.aayushatharva.seqc.Main;
import com.aayushatharva.seqc.utils.Config;
import com.aayushatharva.seqc.utils.Packets;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
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
                    .handler(new PlayerHandler());

            Channel channel = bootstrap.connect(Config.GameAddress).sync().channel();

            while (keepRunning) {
                channel.writeAndFlush(Packets.A2S_PLAYER_CHALLENGE_REQUEST_2).sync();
                try {
                    sleep(Config.UpdateRate);
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

    public void shutdown() {
        this.keepRunning = false;
        this.interrupt();
    }
}
