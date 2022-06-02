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
package com.aayushatharva.seqc.gameserver.a2srules;

import com.aayushatharva.seqc.Handler;
import com.aayushatharva.seqc.gameserver.SplitPacketDecoder;
import com.aayushatharva.seqc.utils.Packets;
import io.netty5.buffer.BufferUtil;
import io.netty5.buffer.api.Buffer;
import io.netty5.buffer.api.internal.Statics;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.channel.SimpleChannelInboundHandler;
import io.netty5.channel.socket.DatagramPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class RulesHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(RulesHandler.class);
    private static final List<Buffer> BUFFER_LIST = new ArrayList<>();

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        // 1. If we receive A2S RULE Challenge then respond back to server with Challenge Code
        try (Buffer buffer = msg.content().copy()) {
            if (Statics.equals(Packets.A2S_CHALLENGE_RESPONSE_HEADER, buffer.readSplit(Packets.A2S_CHALLENGE_RESPONSE_HEADER_LEN))) {
                Buffer response = ctx.bufferAllocator().allocate(Packets.A2S_RULES_REQUEST_HEADER_LEN + Packets.LEN_CODE)
                        .writeBytes(Packets.A2S_RULES_REQUEST_HEADER.copy())
                        .writeBytes(msg.content().readSplit(Packets.LEN_CODE));

                ctx.writeAndFlush(response);
                return;
            }
        }

        // 2. If we receive A2S RULE without challenge then store it into cache directly.
        try (Buffer buffer = msg.content().copy()) {
            if (Statics.equals(Packets.A2S_RULES_RESPONSE_HEADER, buffer.readSplit(Packets.A2S_RULES_RESPONSE_HEADER.readableBytes()))) {
                Handler.INSTANCE.receiveA2sRule(Collections.singletonList(msg.content().copy()));

                logger.debug("New A2S_RULE Update Cached Successfully");
                return;
            }
        }

        // 3. If we receive Split Packet then we will store it into Buffer List
        try (Buffer buffer = msg.content().copy()) {
            if (Statics.equals(Packets.SPLIT_PACKET_HEADER, buffer.readSplit(Packets.SPLIT_PACKET_LEN))) {
                BUFFER_LIST.add(msg.content().copy());

                logger.debug("Received Split A2S_RULE Packet, Current List Size: {}", BUFFER_LIST.size());
                return;
            }
        }
        msg.close();
        logger.error("Received unsupported A2S_RULE Response from Game Server: {}", BufferUtil.hexDump(msg.content()).toUpperCase());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof SplitPacketDecoder.SplitPacketsReceivedEvent) {
            if (!BUFFER_LIST.isEmpty()) {
                Handler.INSTANCE.receiveA2sRule(BUFFER_LIST);
                BUFFER_LIST.clear();
            }
        }
    }
}
