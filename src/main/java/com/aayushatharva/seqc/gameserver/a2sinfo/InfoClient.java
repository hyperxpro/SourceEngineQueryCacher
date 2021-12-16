package com.aayushatharva.seqc.gameserver.a2sinfo;

import com.aayushatharva.seqc.utils.Config;
import com.aayushatharva.seqc.utils.Packets;
import com.aayushatharva.seqc.Main;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.epoll.EpollDatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class InfoClient extends Thread {

    private static final Logger logger = LogManager.getLogger(InfoClient.class);
    private boolean keepRunning = true;

    public InfoClient(String name) {
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
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(1536))
                    .handler(new InfoHandler());

            Channel channel = bootstrap.connect(Config.GameAddress).sync().channel();

            while (keepRunning) {
                channel.writeAndFlush(Packets.A2S_INFO_REQUEST).sync();
                try{
                    sleep(Config.UpdateRate);
                } catch(InterruptedException e){
                    logger.error("Error at InfoClient During Interval Sleep ", e);
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
