package com.aayushatharva.sourcecenginequerycacher;

import com.aayushatharva.sourcecenginequerycacher.utils.ByteArray;
import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import com.aayushatharva.sourcecenginequerycacher.utils.Hex;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.util.Arrays;
import java.util.Random;

class Handler extends SimpleChannelInboundHandler<DatagramPacket> {

    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        Stats.PPS.incrementAndGet();

        // If Request Packet Exceeds 100 Bytes in Length then We'll Drop It
        if (msg.content().readableBytes() > 100) {
            return;
        }

        // Build Packet into Byte Array from ByteBuf
        byte[] Packet = new byte[msg.content().readableBytes()];
        msg.content().readBytes(Packet);

        // A2S INFO Packet
        if (Arrays.equals(Packets.A2S_INFO_REQUEST, Packet)) {
            ctx.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(CacheHub.A2S_INFO), msg.sender())); // Send 'A2S INFO' Packet
        }
        // A2S PLAYER CHALLENGE Packet
        else if (Arrays.equals(Packets.A2S_PLAYER_CHALLENGE_REQUEST_A, Packet) || Arrays.equals(Packets.A2S_PLAYER_CHALLENGE_REQUEST_B, Packet)) {
            // Generate Random Data of 4 Bytes
            byte[] challenge = new byte[4];
            new Random(4L).nextBytes(challenge);

            // Add Challenge to Cache
            CacheHub.CHALLENGE_CACHE.put(Hex.toString(challenge), msg.sender().getAddress().getHostAddress());

            // Send A2S PLAYER CHALLENGE Packet
            ctx.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(ByteArray.joinArrays(Packets.A2S_PLAYER_CHALLENGE_RESPONSE, challenge)), msg.sender()));
        } else if (Packet.length == 9 && Arrays.equals(Arrays.copyOfRange(Packet, 0, 5), Packets.A2S_PLAYER_HEADER)) {
            // Look for Challenge Code in Cache and load Client IP Address Value from it.
            String ipAddressOfClient = CacheHub.CHALLENGE_CACHE.getIfPresent(Hex.toString(Arrays.copyOfRange(Packet, 5, 9)));

            // If Client IP Address Value is not NULL it means we found the Challenge and now we'll validate it
            if (ipAddressOfClient != null) {
                CacheHub.CHALLENGE_CACHE.invalidate(Hex.toString(Arrays.copyOfRange(Packet, 5, 9))); // Invalidate Cache since we found Challenge

                // Match Client Current IP Address against Cache Stored Client IP Address
                if (ipAddressOfClient.equals(msg.sender().getAddress().getHostAddress())) {
                    ctx.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(CacheHub.A2S_PLAYER), msg.sender()));
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
