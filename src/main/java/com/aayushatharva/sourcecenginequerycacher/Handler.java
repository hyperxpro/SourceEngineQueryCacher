package com.aayushatharva.sourcecenginequerycacher;

import com.aayushatharva.sourcecenginequerycacher.utils.ByteArrayUtils;
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

    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {

        if (Config.Stats_PPS) {
            Stats.PPS.incrementAndGet();
        }

        if (Config.Stats_BPS) {
            Stats.BPS.addAndGet(msg.content().readableBytes());
        }

        /*
         * Packet size of 25 bytes and 9 bytes only will be processed rest will dropped.
         *
         * A2S_INFO = 25 Bytes
         * A2S_Player = 9 Bytes
         */
        if (msg.content().readableBytes() == 25 || msg.content().readableBytes() == 9) {

            // Build Packet into Byte Array from ByteBuf
            byte[] Packet = new byte[msg.content().readableBytes()];
            msg.content().readBytes(Packet);

            if (Arrays.equals(Packets.A2S_INFO_REQUEST, Packet)) {
                ctx.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(CacheHub.A2S_INFO.get()), msg.sender()));
            } else if (ByteArrayUtils.startsWith(Packet, Packets.A2S_PLAYER_HEADER)) {

                /*
                 * 1. Packets equals to `A2S_PLAYER_CHALLENGE_REQUEST_A` or `A2S_PLAYER_CHALLENGE_REQUEST_B`
                 * then we'll send response of A2S_Player Challenge Packet.
                 *
                 * 2. Validate A2S_Player Challenge Response and send A2S_Player Packet.
                 */
                if (Arrays.equals(Packet, Packets.A2S_PLAYER_CHALLENGE_REQUEST_A) || Arrays.equals(Packet, Packets.A2S_PLAYER_CHALLENGE_REQUEST_B)) {
                    sendA2SPlayerChallenge(ctx, msg);
                } else {
                    sendA2SPlayerResponse(ctx, msg, Packet);
                }
            }
        }
    }

    private void sendA2SPlayerChallenge(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        // Generate Random Data of 4 Bytes
        byte[] challenge = new byte[4];
        new Random(4L).nextBytes(challenge);

        // Add Challenge to Cache
        CacheHub.CHALLENGE_CACHE.put(Hex.toString(challenge), datagramPacket.sender().getAddress().getHostAddress());

        // Send A2S PLAYER CHALLENGE Packet
        ctx.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(ByteArrayUtils.joinArrays(Packets.A2S_PLAYER_CHALLENGE_RESPONSE, challenge)), datagramPacket.sender()));
    }

    private void sendA2SPlayerResponse(ChannelHandlerContext ctx, DatagramPacket datagramPacket, byte[] Packet) {
        // Look for Challenge Code in Cache and load Client IP Address Value from it.
        String ipAddressOfClient = CacheHub.CHALLENGE_CACHE.getIfPresent(Hex.toString(Arrays.copyOfRange(Packet, 5, 9)));

        // If Client IP Address Value is not NULL it means we found the Challenge and now we'll validate it.
        if (ipAddressOfClient != null) {
            CacheHub.CHALLENGE_CACHE.invalidate(Hex.toString(Arrays.copyOfRange(Packet, 5, 9))); // Invalidate Cache since we found Challenge

            // Match Client Current IP Address against Cache Stored Client IP Address
            if (ipAddressOfClient.equals(datagramPacket.sender().getAddress().getHostAddress())) {
                ctx.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(CacheHub.A2S_PLAYER.get()), datagramPacket.sender()));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}
