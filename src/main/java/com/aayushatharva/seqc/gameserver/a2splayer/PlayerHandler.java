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
package com.aayushatharva.seqc.gameserver.a2splayer;

import com.aayushatharva.seqc.Handler;
import com.aayushatharva.seqc.gameserver.SplitPacketDecoder;
import com.aayushatharva.seqc.utils.ExtraBufferUtil;
import io.netty5.buffer.BufferUtil;
import io.netty5.buffer.api.Buffer;
import io.netty5.buffer.api.Send;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.channel.SimpleChannelInboundHandler;
import io.netty5.channel.socket.DatagramPacket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.aayushatharva.seqc.utils.Packets.A2S_CHALLENGE_RESPONSE_HEADER;
import static com.aayushatharva.seqc.utils.Packets.A2S_CHALLENGE_RESPONSE_HEADER_LEN;
import static com.aayushatharva.seqc.utils.Packets.A2S_PLAYER_REQUEST_HEADER;
import static com.aayushatharva.seqc.utils.Packets.A2S_PLAYER_REQUEST_HEADER_LEN;
import static com.aayushatharva.seqc.utils.Packets.A2S_PLAYER_RESPONSE_HEADER;
import static com.aayushatharva.seqc.utils.Packets.LEN_CODE;
import static com.aayushatharva.seqc.utils.Packets.SPLIT_PACKET_HEADER;

final class PlayerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(PlayerHandler.class);
    private static final List<Buffer> BUFFER_LIST = new ObjectArrayList<>();

    PlayerHandler() {
        super(false);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) {
        boolean release = true;
        try {
            Buffer buffer = msg.content();

            // 1. If we receive A2S PLAYER Challenge then respond back to server with Challenge Code
            if (ExtraBufferUtil.contains(A2S_CHALLENGE_RESPONSE_HEADER, buffer)) {
                Buffer response = ctx.bufferAllocator().allocate(A2S_PLAYER_REQUEST_HEADER_LEN + LEN_CODE)
                        .writeBytes(A2S_PLAYER_REQUEST_HEADER.copy())
                        .writeBytes(buffer.skipReadable(A2S_CHALLENGE_RESPONSE_HEADER_LEN));

                ctx.writeAndFlush(response);
                return;
            }

            // 2. If we receive A2S PLAYER without challenge then store it into cache directly.
            if (ExtraBufferUtil.contains(A2S_PLAYER_RESPONSE_HEADER, buffer)) {
                Handler.INSTANCE.receiveA2sPlayer(Collections.singletonList(msg.content().send()));

                logger.debug("New A2S_PLAYER Update Cached Successfully");
                release = false;
                return;
            }

            // 3. If we receive Split Packet then we will store it into Buffer List
            if (ExtraBufferUtil.contains(SPLIT_PACKET_HEADER, buffer)) {
                BUFFER_LIST.add(msg.content());

                logger.debug("Received Split A2S_PLAYER Packet, Current List Size: {}", BUFFER_LIST.size());
                release = false;
                return;
            }

            logger.error("Received unsupported A2S_PLAYER Response from Game Server: {}", BufferUtil.hexDump(buffer).toUpperCase());
        } finally {
            if (release) {
                msg.close();
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof SplitPacketDecoder.SplitPacketsReceivedEvent) {
            if (!BUFFER_LIST.isEmpty()) {
                List<Send<Buffer>> sends = new ArrayList<>();
                for (Buffer buffer : BUFFER_LIST) {
                    sends.add(buffer.send());
                }
                Handler.INSTANCE.receiveA2sPlayer(sends);
                BUFFER_LIST.clear();
            }
        }
    }
}
