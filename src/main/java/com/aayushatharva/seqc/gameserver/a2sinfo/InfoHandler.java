package com.aayushatharva.seqc.gameserver.a2sinfo;

import com.aayushatharva.seqc.utils.Cache;
import com.aayushatharva.seqc.utils.Packets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class InfoHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(InfoHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        if (ByteBufUtil.equals(Packets.A2S_CHALLENGE_RESPONSE_HEADER, datagramPacket.content().slice(0, Packets.A2S_CHALLENGE_RESPONSE_HEADER_LEN))) {
            ByteBuf responseBuf = ctx.alloc().buffer()
                    .writeBytes(Packets.A2S_INFO_REQUEST)
                    .writeBytes(datagramPacket.content().slice(Packets.A2S_CHALLENGE_RESPONSE_CODE_POS, Packets.LEN_CODE));

            ctx.writeAndFlush(responseBuf, ctx.voidPromise());
        } else if (ByteBufUtil.equals(Packets.A2S_INFO_RESPONSE_HEADER, datagramPacket.content().slice(0, Packets.A2S_INFO_RESPONSE_HEADER_LEN))) {
            // Set new Packet Data
            Cache.A2S_INFO.clear().writeBytes(datagramPacket.content());

            logger.debug("New A2SInfo Update Cached Successfully; Size: {}", Cache.A2S_INFO.readableBytes());
        } else if (ByteBufUtil.equals(Packets.A2S_INFO_RESPONSE_HEADER_SPLIT, datagramPacket.content().slice(0, Packets.A2S_INFO_RESPONSE_HEADER_SPLIT_LEN))) {
            // Set new Packet Data
            // todo: Make sure we get and send all packets.
            Cache.A2S_INFO.clear().writeBytes(datagramPacket.content());

            logger.debug("[SPLIT PACKET] New A2SInfo Update Cached Successfully; Size: {}", Cache.A2S_INFO.readableBytes());
        } else {
            logger.error("Received unsupported A2S Info Response from Game Server: {}", ByteBufUtil.hexDump(datagramPacket.content()).toUpperCase());
        }
    }
}
