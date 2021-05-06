package com.aayushatharva.sourcecenginequerycacher.gameserver.a2sinfo;

import com.aayushatharva.sourcecenginequerycacher.Main;
import com.aayushatharva.sourcecenginequerycacher.utils.Config;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;
import io.netty.bootstrap.Bootstrap;
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
                    .option(ChannelOption.ALLOCATOR, Main.BYTE_BUF_ALLOCATOR)
                    .option(ChannelOption.SO_SNDBUF, Config.SendBufferSize)
                    .option(ChannelOption.SO_RCVBUF, Config.ReceiveBufferSize)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(Config.FixedReceiveAllocatorBufferSize))
                    .handler(new InfoHandler());

            Channel channel = bootstrap.connect(Config.GameServer).sync().channel();

            while (keepRunning) {
                channel.writeAndFlush(Packets.A2S_INFO_REQUEST.retainedDuplicate()).sync();
                sleep(Config.GameUpdateInterval);
            }

            channel.closeFuture().sync();
        } catch (Exception ex) {
            logger.atError().withThrowable(ex).log("Error occurred");
        }
    }

    public void shutdown() {
        this.interrupt();
        keepRunning = false;
    }
}
