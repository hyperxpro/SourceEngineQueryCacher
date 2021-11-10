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

        if (ByteBufUtil.equals(Packets.A2S_CHALLENGE_RESPONSE, datagramPacket.content().slice(0, Packets.A2S_CHALLENGE_RESPONSE.readableBytes()))) {
            ByteBuf responseBuf = ctx.alloc().buffer()
                    .writeBytes(Packets.A2S_PLAYER_REQUEST_HEADER.retainedDuplicate())
                    .writeBytes(datagramPacket.content().slice(Packets.A2S_CHALLENGE_RESPONSE_CODE_POS, Packets.LEN_CODE));

            ctx.writeAndFlush(responseBuf, ctx.voidPromise());
            logger.debug("Fetching A2SPlayer Update with Challenge Code {}", ByteBufUtil.hexDump(datagramPacket.content().slice(Packets.A2S_CHALLENGE_RESPONSE_CODE_POS, Packets.LEN_CODE)));
        } else if (ByteBufUtil.equals(Packets.A2S_PLAYER_RESPONSE_HEADER, datagramPacket.content().slice(0, Packets.A2S_PLAYER_RESPONSE_HEADER.readableBytes()))) {
            // Set new Packet Data
            CacheHub.A2S_PLAYER.clear().writeBytes(datagramPacket.content());

            logger.debug("New A2SPlayer Update Cached Successfully; Size: {}", CacheHub.A2S_INFO.readableBytes());
        } else {
            logger.error("Received unsupported A2S Player Response from Game Server: {}", ByteBufUtil.hexDump(datagramPacket.content()));
        }
    }
}
