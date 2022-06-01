/*
 * This file is part of SourceEngineQueryCacher. [https://github.com/hyperxpro/SourceEngineQueryCacher]
 * Copyright (c) 2020-2022 Aayush Atharva
 *
 * SourceEngineQueryCacher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SourceEngineQueryCacher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SourceEngineQueryCacher.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.aayushatharva.seqc.gameserver.a2sinfo;

import com.aayushatharva.seqc.Handler;
import com.aayushatharva.seqc.gameserver.SplitPacketDecoder;
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
    private static final List<Send<Buffer>> BUFFER_LIST = new ArrayList<>();

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) {
        // 1. If we receive A2S INFO Challenge then respond back to server with Challenge Code
        try (Buffer buffer = msg.content().copy()) {
            if (Statics.equals(Packets.A2S_CHALLENGE_RESPONSE_HEADER, buffer.readSplit(Packets.A2S_CHALLENGE_RESPONSE_HEADER_LEN))) {
                Buffer response = ctx.bufferAllocator().allocate(Packets.A2S_INFO_REQUEST_LEN + Packets.LEN_CODE)
                        .writeBytes(Packets.A2S_INFO_REQUEST.copy())
                        .writeBytes(buffer.readSplit(Packets.LEN_CODE));

                ctx.writeAndFlush(response);
                return;
            }
        }

        // 2. If we receive A2S INFO without challenge then store it into cache directly.
        try (Buffer buffer = msg.content().copy()) {
            if (Statics.equals(Packets.A2S_INFO_RESPONSE_HEADER, buffer.readSplit(Packets.A2S_INFO_RESPONSE_HEADER_LEN))) {
                Handler.INSTANCE.receiveA2sInfo(List.of(msg.content().copy().send()));

                logger.debug("New A2S_INFO Update Cached Successfully");
                return;
            }
        }

        // 3. If we receive Split Packet then we will store it into Buffer List
        try (Buffer buffer = msg.content().copy()) {
            if (Statics.equals(Packets.SPLIT_PACKET_HEADER, buffer.readSplit(Packets.SPLIT_PACKET_LEN))) {
                BUFFER_LIST.add(msg.content().copy().send());

                logger.debug("Received Split A2S_INFO Packet, Current List Size: {}", BUFFER_LIST.size());
                return;
            }
        }

        logger.error("Received unsupported A2S_INFO Response from Game Server: {}", BufferUtil.hexDump(msg.content()).toUpperCase());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof SplitPacketDecoder.SplitPacketsReceivedEvent) {
            if (!BUFFER_LIST.isEmpty()) {
                Handler.INSTANCE.receiveA2sInfo(BUFFER_LIST);
                BUFFER_LIST.clear();
            }
        }
    }
}
