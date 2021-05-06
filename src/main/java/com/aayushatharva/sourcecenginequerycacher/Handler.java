package com.aayushatharva.sourcecenginequerycacher;

import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import com.aayushatharva.sourcecenginequerycacher.utils.Config;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Random;
import java.util.SplittableRandom;

@ChannelHandler.Sharable
final class Handler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(Handler.class);
    private static final SplittableRandom RANDOM = new SplittableRandom();

    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {

        if (Config.Stats_PPS) {
            Stats.PPS.incrementAndGet();
        }

        if (Config.Stats_bPS) {
            Stats.BPS.addAndGet(datagramPacket.content().readableBytes());
        }

        /*
         * If A2S_INFO or A2S_PLAYER is null or 0 bytes, drop request because we've nothing to reply.
         */
        if (CacheHub.A2S_INFO == null || CacheHub.A2S_INFO.readableBytes() == 0 ||
                CacheHub.A2S_PLAYER == null || CacheHub.A2S_PLAYER.readableBytes() == 0) {
            logger.error("Dropping query request because Cache is not ready. A2S_INFO: {}, A2S_PLAYER: {}",
                    CacheHub.A2S_INFO, CacheHub.A2S_PLAYER);
            return;
        }

        /*
         * Packet size of 25 bytes and 9 bytes only will be processed rest will dropped.
         *
         * A2S_INFO = 25 Bytes
         * A2S_Player = 9 Bytes
         */
        if (datagramPacket.content().readableBytes() == 25 || datagramPacket.content().readableBytes() == 9) {
            if (ByteBufUtil.equals(Packets.A2S_INFO_REQUEST, datagramPacket.content())) {
                ctx.writeAndFlush(new DatagramPacket(CacheHub.A2S_INFO.retainedDuplicate(), datagramPacket.sender()));
                return;
            } else if (ByteBufUtil.equals(Packets.A2S_PLAYER_REQUEST_HEADER, datagramPacket.content().slice(0, 5))) {

                /*
                 * 1. Packets equals to `A2S_PLAYER_CHALLENGE_REQUEST_1` or `A2S_PLAYER_CHALLENGE_REQUEST_2`
                 * then we'll send response of A2S_Player Challenge Packet.
                 *
                 * 2. Validate A2S_Player Challenge Response and send A2S_Player Packet.
                 */
                if (ByteBufUtil.equals(datagramPacket.content(), Packets.A2S_PLAYER_CHALLENGE_REQUEST_1) ||
                        ByteBufUtil.equals(datagramPacket.content(), Packets.A2S_PLAYER_CHALLENGE_REQUEST_2)) {
                    sendA2SPlayerChallenge(ctx, datagramPacket);
                } else {
                    sendA2SPlayerResponse(ctx, datagramPacket, ByteBufUtil.getBytes(datagramPacket.content()));
                }
                return;
            }
        }

        dropLog(datagramPacket);
    }

    private void sendA2SPlayerChallenge(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        // Generate Random Data of 4 Bytes
        byte[] challenge = new byte[4];
        RANDOM.nextBytes(challenge);

        // Add Challenge to Cache
        CacheHub.CHALLENGE_CACHE.put(toHexString(challenge), datagramPacket.sender().getAddress().getHostAddress());

        // Send A2S PLAYER CHALLENGE Packet
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(Packets.A2S_PLAYER_CHALLENGE_RESPONSE.retainedDuplicate());
        byteBuf.writeBytes(challenge);
        ctx.writeAndFlush(new DatagramPacket(byteBuf, datagramPacket.sender()));
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
                ctx.writeAndFlush(new DatagramPacket(CacheHub.A2S_PLAYER.retainedDuplicate(), datagramPacket.sender()));
            }
        } else {
            logger.debug("Invalid Challenge Code received from {}:{} [REQUEST DROPPED]",
                    datagramPacket.sender().getAddress().getHostAddress(), datagramPacket.sender().getPort());
        }
    }

    private void dropLog(DatagramPacket datagramPacket) {
        logger.debug("Dropping Packet of Length {} bytes from {}:{}", datagramPacket.content().readableBytes(),
                datagramPacket.sender().getAddress().getHostAddress(), datagramPacket.sender().getPort());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Caught Error", cause);
    }

    /**
     * Convert Byte Array into Hex String
     *
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
