package com.aayushatharva.sourcecenginequerycacher;

import com.aayushatharva.sourcecenginequerycacher.utils.ByteArrayUtils;
import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import com.aayushatharva.sourcecenginequerycacher.utils.Config;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Random;

class Handler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(Handler.class);

    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {

        if (Config.Stats_PPS) {
            Stats.PPS.incrementAndGet();
            logger.atDebug().log("Incrementing PPS count by 1");
        }

        if (Config.Stats_bPS) {
            Stats.BPS.addAndGet(datagramPacket.content().readableBytes());
            logger.atDebug().log("Incrementing bPS count by " + datagramPacket.content().readableBytes()  + " bytes");
        }

        /*
         * Packet size of 25 bytes and 9 bytes only will be processed rest will dropped.
         *
         * A2S_INFO = 25 Bytes
         * A2S_Player = 9 Bytes
         */
        if (datagramPacket.content().readableBytes() == 25 || datagramPacket.content().readableBytes() == 9) {

            // Build Packet into Byte Array from ByteBuf
            byte[] Packet = new byte[datagramPacket.content().readableBytes()];
            datagramPacket.content().readBytes(Packet);

            // Log at Debug
            logger.atDebug().log("Received Packet of Length: " + Packet.length + " bytes from " + datagramPacket.sender().getAddress().getHostAddress() + ":" + datagramPacket.sender().getPort());

            if (Arrays.equals(Packets.A2S_INFO_REQUEST, Packet)) {
                logger.atDebug().log("Sending A2S_INFO Packet to: " + datagramPacket.sender().getAddress().getHostAddress() + ":" + datagramPacket.sender().getPort());
                ctx.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(CacheHub.A2S_INFO.get()), datagramPacket.sender()));
            } else if (ByteArrayUtils.startsWith(Packet, Packets.A2S_PLAYER_HEADER)) {

                /*
                 * 1. Packets equals to `A2S_PLAYER_CHALLENGE_REQUEST_A` or `A2S_PLAYER_CHALLENGE_REQUEST_B`
                 * then we'll send response of A2S_Player Challenge Packet.
                 *
                 * 2. Validate A2S_Player Challenge Response and send A2S_Player Packet.
                 */
                if (Arrays.equals(Packet, Packets.A2S_PLAYER_CHALLENGE_REQUEST_A) || Arrays.equals(Packet, Packets.A2S_PLAYER_CHALLENGE_REQUEST_B)) {
                    sendA2SPlayerChallenge(ctx, datagramPacket);
                } else {
                    sendA2SPlayerResponse(ctx, datagramPacket, Packet);
                }
            }
        } else {
            logger.atDebug().log("Dropping Packet of Length: " + datagramPacket.content().readableBytes() + " bytes from " + datagramPacket.sender().getAddress().getHostAddress() + ":" + datagramPacket.sender().getPort());
        }
    }

    private void sendA2SPlayerChallenge(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        // Generate Random Data of 4 Bytes
        byte[] challenge = new byte[4];
        new Random(4L).nextBytes(challenge);

        // Add Challenge to Cache
        CacheHub.CHALLENGE_CACHE.put(toHexString(challenge), datagramPacket.sender().getAddress().getHostAddress());
        
        // Log at Debug
        logger.atDebug().log("Sending A2S_Challenge Player Packet to: " + datagramPacket.sender().getAddress().getHostAddress() + ":" + datagramPacket.sender().getPort());

        // Send A2S PLAYER CHALLENGE Packet
        ctx.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(ByteArrayUtils.joinArrays(Packets.A2S_PLAYER_CHALLENGE_RESPONSE, challenge)), datagramPacket.sender()));
    }

    private void sendA2SPlayerResponse(ChannelHandlerContext ctx, DatagramPacket datagramPacket, byte[] Packet) {
        // Look for Challenge Code in Cache and load Client IP Address Value from it.
        String ipAddressOfClient = CacheHub.CHALLENGE_CACHE.getIfPresent(toHexString(Arrays.copyOfRange(Packet, 5, 9)));

        // If Client IP Address Value is not NULL it means we found the Challenge and now we'll validate it.
        if (ipAddressOfClient != null) {
            // Invalidate Cache since we found Challenge
            CacheHub.CHALLENGE_CACHE.invalidate(toHexString(Arrays.copyOfRange(Packet, 5, 9)));

            // Match Client Current IP Address against Cache Stored Client IP Address
            if (ipAddressOfClient.equals(datagramPacket.sender().getAddress().getHostAddress())) {
                logger.atDebug().log("Sending A2S_PLAYER Packet to: " + datagramPacket.sender().getAddress().getHostAddress() + ":" + datagramPacket.sender().getPort());
                ctx.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(CacheHub.A2S_PLAYER.get()), datagramPacket.sender()));
            }
        } else {
            logger.atDebug().log("Invalid Challenge Code received from: " + datagramPacket.sender().getAddress().getHostAddress() + ":" + datagramPacket.sender().getPort() + " [REQUEST DROPPED]");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    /**
     * Convert Byte Array into Hex String
     * @param bytes Byte Array
     * @return Hex String
     */
    private String toHexString(final byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = "0123456789ABCDEF".toCharArray()[v >>> 4];
            hexChars[j * 2 + 1] = "0123456789ABCDEF".toCharArray()[v & 0x0F];
        }
        return new String(hexChars);
    }
}
