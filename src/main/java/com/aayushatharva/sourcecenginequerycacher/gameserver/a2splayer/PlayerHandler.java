package com.aayushatharva.sourcecenginequerycacher.gameserver.a2splayer;

import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class PlayerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(PlayerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {

        if (ByteBufUtil.equals(Packets.A2S_PLAYER_CHALLENGE_RESPONSE, datagramPacket.content().slice(0, 5))) {
            ByteBuf byteBuf = ctx.alloc().buffer()
                    .writeBytes(Packets.A2S_PLAYER_REQUEST_HEADER.retainedDuplicate())
                    .writeBytes(datagramPacket.content().slice(5, 4));

            ctx.channel().writeAndFlush(new DatagramPacket(byteBuf, datagramPacket.sender()));
        } else if (ByteBufUtil.equals(Packets.A2S_PLAYER_RESPONSE_HEADER, datagramPacket.content().slice(0, 5))) {
            // Set new Packet Data
            CacheHub.A2S_PLAYER.clear().writeBytes(datagramPacket.content());

            logger.atDebug().log("New A2SPlayer Update Cached Successfully");
        } else {
            logger.atError().log("Received unsupported A2S Player Response from Game Server: {}", ByteBufUtil.hexDump(datagramPacket.content()));
        }
    }
}
