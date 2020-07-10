package com.aayushatharva.sourcecenginequerycacher.gameserver.a2splayer;

import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;
import com.aayushatharva.sourcecenginequerycacher.utils.Utils;
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
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) {

        if (ByteBufUtil.equals(Packets.A2S_PLAYER_CHALLENGE_RESPONSE, datagramPacket.content().slice(0, 5))) {
            ByteBuf byteBuf = channelHandlerContext.alloc().directBuffer()
                    .writeBytes(Packets.A2S_PLAYER_REQUEST_HEADER.copy())
                    .writeBytes(datagramPacket.content().slice(5, 4));
            channelHandlerContext.channel().writeAndFlush(new DatagramPacket(byteBuf, datagramPacket.sender()));
        } else if (ByteBufUtil.equals(Packets.A2S_PLAYER_RESPONSE_HEADER, datagramPacket.content().slice(0, 5))){
            // Release the ByteBuf
            Utils.safeRelease(CacheHub.A2S_PLAYER.get());
            CacheHub.A2S_PLAYER.set(datagramPacket.content().copy());

            logger.atDebug().log("New A2SPlayer Update Cached Successfully");
        } else {
            logger.atError().log("Received unsupported A2S Player Response from Game Server: {}", ByteBufUtil.hexDump(datagramPacket.content()));
        }
    }
}
