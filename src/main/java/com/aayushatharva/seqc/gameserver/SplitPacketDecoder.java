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
package com.aayushatharva.seqc.gameserver;

import com.aayushatharva.seqc.utils.Packets;
import io.netty5.buffer.api.Buffer;
import io.netty5.buffer.api.internal.Statics;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.channel.socket.DatagramPacket;
import io.netty5.handler.codec.MessageToMessageDecoder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public final class SplitPacketDecoder extends MessageToMessageDecoder<DatagramPacket> {
    private final List<Buffer> BUFFER_LIST = new ObjectArrayList<>();

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg) {
        try (Buffer buffer = msg.content().copy()) {
            // We have received Split Packet.
            if (Statics.equals(Packets.SPLIT_PACKET_HEADER, buffer.readSplit(Packets.SPLIT_PACKET_LEN))) {
                int id = buffer.readInt();                                  // Read ID
                int total = Integer.reverseBytes(buffer.readByte());        // Total number of Packets
                int packetNumber = Integer.reverseBytes(buffer.readByte()); // Packet number

                BUFFER_LIST.add(packetNumber, msg.content());

                // If Buffer List size has reached total number of packets then
                // we will send Buffers List to next Handler.
                if (BUFFER_LIST.size() == total) {
                    // Forward all Buffers to next Handler and fire
                    // 'fireUserEventTriggered#SplitPacketsReceivedEvent.INSTANCE' to
                    // indicate that all split packets have been received
                    BUFFER_LIST.forEach(ctx::fireChannelRead);
                    ctx.fireUserEventTriggered(SplitPacketsReceivedEvent.INSTANCE);
                    BUFFER_LIST.clear();
                }
            } else {
                ctx.fireChannelRead(msg);
            }
        }
    }

    public static final class SplitPacketsReceivedEvent {
        public static final SplitPacketsReceivedEvent INSTANCE = new SplitPacketsReceivedEvent();

        private SplitPacketsReceivedEvent() {
            // Prevent outside initialization
        }
    }
}
