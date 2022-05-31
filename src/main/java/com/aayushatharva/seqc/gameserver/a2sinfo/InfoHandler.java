package com.aayushatharva.seqc.gameserver.a2sinfo;

import com.aayushatharva.seqc.Handler;
import com.aayushatharva.seqc.utils.Packets;
import io.netty5.buffer.BufferUtil;
import io.netty5.buffer.api.Buffer;
import io.netty5.buffer.api.Send;
import io.netty5.buffer.api.internal.Statics;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.channel.SimpleChannelInboundHandler;
import io.netty5.channel.socket.DatagramPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

final class InfoHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(InfoHandler.class);

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        // 1. If we receive A2S INFO Challenge then respond back to server with Challenge Code
        // 2. If we receive A2S INFO without challenge then store it into cache directly.
        if (Statics.equals(Packets.A2S_CHALLENGE_RESPONSE_HEADER, datagramPacket.content().readSplit(Packets.A2S_CHALLENGE_RESPONSE_HEADER_LEN))) {
            Buffer response = ctx.bufferAllocator().allocate(Packets.A2S_INFO_REQUEST_LEN + Packets.LEN_CODE)
                    .writeBytes(Packets.A2S_INFO_REQUEST.copy())
                    .writeBytes(datagramPacket.content().readSplit(Packets.LEN_CODE));

            ctx.writeAndFlush(response);
        } else if (Statics.equals(Packets.A2S_INFO_RESPONSE_HEADER, datagramPacket.content().readSplit(Packets.A2S_INFO_RESPONSE_HEADER_LEN))) {
            // If Buffer List size is more than 0 then we have pending split responses to process
            // before we process this final response.
            if (BUFFER_LIST.size() > 0) {

                return;
            }

            logger.debug("New A2SInfo Update Cached Successfully");
        } else if (Statics.equals(Packets.A2S_INFO_RESPONSE_HEADER_SPLIT, datagramPacket.content().readSplit(Packets.A2S_INFO_RESPONSE_HEADER_SPLIT_LEN))) {


        } else {
            logger.error("Received unsupported A2S Info Response from Game Server: {}", BufferUtil.hexDump(datagramPacket.content()).toUpperCase());
        }
    }
}
