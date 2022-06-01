package com.aayushatharva.seqc.gameserver;

import com.aayushatharva.seqc.utils.Packets;
import io.netty5.buffer.api.Buffer;
import io.netty5.buffer.api.internal.Statics;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.channel.socket.DatagramPacket;
import io.netty5.handler.codec.MessageToMessageDecoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SplitPacketDecoder extends MessageToMessageDecoder<DatagramPacket> {
    private final List<Buffer> BUFFER_LIST = new ArrayList<>();

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
