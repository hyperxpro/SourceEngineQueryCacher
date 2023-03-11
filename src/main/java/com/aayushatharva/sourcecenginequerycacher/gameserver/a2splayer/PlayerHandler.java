package com.aayushatharva.sourcecenginequerycacher.gameserver.a2splayer;

import com.aayushatharva.sourcecenginequerycacher.utils.Cache;
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

        if (ByteBufUtil.equals(Packets.A2S_CHALLENGE_RESPONSE_HEADER, datagramPacket.content().slice(0, Packets.A2S_CHALLENGE_RESPONSE_HEADER_LEN))) {
            ByteBuf responseBuf = ctx.alloc().buffer()
                    .writeBytes(Packets.A2S_PLAYER_REQUEST_HEADER.copy())
                    .writeBytes(datagramPacket.content().slice(Packets.A2S_CHALLENGE_RESPONSE_CODE_POS, Packets.LEN_CODE));

            ctx.writeAndFlush(responseBuf, ctx.voidPromise());
            logger.debug("Fetching A2SPlayer Update with Challenge Code {}",
                    ByteBufUtil.hexDump(datagramPacket.content().slice(Packets.A2S_CHALLENGE_RESPONSE_CODE_POS, Packets.LEN_CODE)).toUpperCase());
        } else if (ByteBufUtil.equals(Packets.A2S_PLAYER_RESPONSE_HEADER, datagramPacket.content().slice(0, Packets.A2S_PLAYER_RESPONSE_HEADER.readableBytes()))) {
            // Set new Packet Data
            Cache.A2S_PLAYER.clear().writeBytes(datagramPacket.content());

            logger.debug("New A2SPlayer Update Cached Successfully; Size: {}", Cache.A2S_PLAYER.readableBytes());
        } else if (ByteBufUtil.equals(Packets.A2S_PLAYER_RESPONSE_HEADER_SPLIT, datagramPacket.content().slice(0, Packets.A2S_PLAYER_RESPONSE_HEADER_SPLIT.readableBytes()))) {
            // Set new Packet Data
            Cache.A2S_PLAYER.clear().writeBytes(datagramPacket.content());

            logger.debug("[SPLIT PACKET] New A2SPlayer Update Cached Successfully; Size: {}; Content: {}", Cache.A2S_PLAYER.readableBytes(), ByteBufUtil.hexDump(datagramPacket.content()).toUpperCase());
        } else {
            logger.error("Received unsupported A2S Player Response from Game Server: {}", ByteBufUtil.hexDump(datagramPacket.content()).toUpperCase());
        }
    }
}
