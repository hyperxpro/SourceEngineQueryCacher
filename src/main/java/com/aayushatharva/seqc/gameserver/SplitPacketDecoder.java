package com.aayushatharva.seqc.gameserver;

import com.aayushatharva.seqc.Handler;
import io.netty5.buffer.api.Buffer;
import io.netty5.buffer.api.Send;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.channel.socket.DatagramPacket;
import io.netty5.handler.codec.MessageToMessageDecoder;

import java.util.ArrayList;
import java.util.List;

public final class SplitPacketDecoder extends MessageToMessageDecoder<DatagramPacket> {
    private static final List<Buffer> BUFFER_LIST = new ArrayList<>();

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        Buffer buffer = msg.content();
        int id = buffer.readInt();                                  // Read ID
        int total = Integer.reverseBytes(buffer.readByte());        // Total number of Packets
        int packetNumber = Integer.reverseBytes(buffer.readByte()); // Packet number
        buffer.resetOffsets();
        BUFFER_LIST.add(msg.content().resetOffsets().copy());

        // If Buffer List size has reached total number of packets then
        //
        if (BUFFER_LIST.size() == total) {
            List<Send<Buffer>> buffers = new ArrayList<>();
            for (Buffer buf : BUFFER_LIST) {
                buffers.add(buf.resetOffsets().send());
            }
            buffers.add(datagramPacket.content().resetOffsets().copy().send());
            Handler.INSTANCE.receiveA2sInfo(buffers);

            BUFFER_LIST.clear();
            buffers.clear();
        } else {

        }
    }
}
