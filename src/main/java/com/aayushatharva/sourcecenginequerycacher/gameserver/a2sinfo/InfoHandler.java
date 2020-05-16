package com.aayushatharva.sourcecenginequerycacher.gameserver.a2sinfo;

import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;
import com.aayushatharva.sourcecenginequerycacher.utils.Utils;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class InfoHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(InfoHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) {

        if (ByteBufUtil.equals(Packets.A2S_INFO_RESPONSE_HEADER, datagramPacket.content().slice(0, 5))) {
            // Release the ByteBuf
            Utils.safeRelease(CacheHub.A2S_INFO.get());
            CacheHub.A2S_INFO.set(datagramPacket.content().copy());

            logger.atDebug().log("New A2SInfo Update Cached Successfully");
        } else {
            logger.atError().log("Received unsupported A2S Info Response from Game Server: {}",
                    ByteBufUtil.hexDump(datagramPacket.content()));
        }
    }
}
