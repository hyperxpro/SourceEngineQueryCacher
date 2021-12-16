package com.aayushatharva.seqc;

import com.aayushatharva.seqc.utils.Cache;
import com.aayushatharva.seqc.utils.Config;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.SplittableRandom;

import static com.aayushatharva.seqc.utils.Packets.A2S_CHALLENGE_RESPONSE_HEADER;
import static com.aayushatharva.seqc.utils.Packets.A2S_INFO_CODE_POS;
import static com.aayushatharva.seqc.utils.Packets.A2S_INFO_REQUEST;
import static com.aayushatharva.seqc.utils.Packets.A2S_INFO_REQUEST_LEN;
import static com.aayushatharva.seqc.utils.Packets.A2S_PLAYER_CHALLENGE_REQUEST_1;
import static com.aayushatharva.seqc.utils.Packets.A2S_PLAYER_CHALLENGE_REQUEST_2;
import static com.aayushatharva.seqc.utils.Packets.A2S_PLAYER_CODE_POS;
import static com.aayushatharva.seqc.utils.Packets.A2S_PLAYER_REQUEST_HEADER;
import static com.aayushatharva.seqc.utils.Packets.A2S_PLAYER_REQUEST_HEADER_LEN;
import static com.aayushatharva.seqc.utils.Packets.A2S_RULES_CHALLENGE_REQUEST_1;
import static com.aayushatharva.seqc.utils.Packets.A2S_RULES_CHALLENGE_REQUEST_2;
import static com.aayushatharva.seqc.utils.Packets.A2S_RULES_CODE_POS;
import static com.aayushatharva.seqc.utils.Packets.A2S_RULES_REQUEST_HEADER;
import static com.aayushatharva.seqc.utils.Packets.A2S_RULES_REQUEST_HEADER_LEN;
import static com.aayushatharva.seqc.utils.Packets.LEN_CODE;

@ChannelHandler.Sharable
final class Handler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(Handler.class);
    private static final SplittableRandom RANDOM = new SplittableRandom();

    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        int pckLength = packet.content().readableBytes();

        if (Config.Stats_PPS)
            Stats.PPS.incrementAndGet();

        if (Config.Stats_BPS)
            Stats.BPS.addAndGet(pckLength);

        /*
         * If A2S_INFO or A2S_PLAYER is not readable or If A2S_RULE is enabled but not readable,
         * drop request because we've nothing to reply.
         */
        if (!Cache.A2S_INFO.isReadable() || !Cache.A2S_PLAYER.isReadable() || !(Config.EnableA2SRule && !Cache.A2S_RULES.isReadable())) {
            logger.error("Dropping query request because Cache is not ready. A2S_INFO: {}, A2S_PLAYER: {}, A2S_RULES: {}",
                    Cache.A2S_INFO, Cache.A2S_PLAYER, Cache.A2S_RULES);
            return;
        }

        /*
         * Packet size of 25, 29 bytes and 9 bytes only will be processed rest will be dropped.
         *
         * A2S_INFO = 25 Bytes, 29 bytes with padded challenge code
         * A2S_Player = 9 Bytes
         * A2S_RULES = 9 Bytes
         */
        if (pckLength == 9 || pckLength == 25 || pckLength == 29) {

            if (Config.EnableA2SRule && ByteBufUtil.equals(A2S_RULES_REQUEST_HEADER, packet.content().slice(0, A2S_RULES_REQUEST_HEADER_LEN))) {

                /*
                 * 1. Packet equals `A2S_RULES_CHALLENGE_REQUEST_1` or `A2S_RULES_CHALLENGE_REQUEST_2`
                 * then we'll send response of A2S_Challenge Packet.
                 */
                if (ByteBufUtil.equals(packet.content(), A2S_RULES_CHALLENGE_REQUEST_2) ||
                        ByteBufUtil.equals(packet.content(), A2S_RULES_CHALLENGE_REQUEST_1)) {
                    sendA2SChallenge(ctx, packet);
                } else {
                    // 2. Validate A2S_RULES Challenge Response and send A2S_Rules Packet.
                    sendA2SRulesResponse(ctx, packet);
                }
                return;
            } else if (ByteBufUtil.equals(A2S_PLAYER_REQUEST_HEADER, packet.content().slice(0, A2S_PLAYER_REQUEST_HEADER_LEN))) {

                /*
                 * 1. Packet equals to `A2S_PLAYER_CHALLENGE_REQUEST_1` or `A2S_PLAYER_CHALLENGE_REQUEST_2`
                 * then we'll send response of A2S_Player Challenge Packet.
                 */
                if (ByteBufUtil.equals(packet.content(), A2S_PLAYER_CHALLENGE_REQUEST_2) ||
                        ByteBufUtil.equals(packet.content(), A2S_PLAYER_CHALLENGE_REQUEST_1)) {
                    sendA2SChallenge(ctx, packet);
                } else {
                    // 2. Validate A2S_Player Challenge Response and send A2S_Player Packet.
                    sendA2SPlayerResponse(ctx, packet);
                }
                return;
            } else if (ByteBufUtil.equals(A2S_INFO_REQUEST, packet.content().slice(0, A2S_INFO_REQUEST_LEN))) {

                /*
                 * 1. Packet equals to `A2S_INFO_REQUEST` with length==25 (A2S_INFO without challenge code)
                 * then we'll send response of A2S_Challenge Packet.
                 *
                 * 2. Validate A2S_INFO Challenge Response (length==29) and send A2S_INFO Packet.
                 */
                if (pckLength == A2S_INFO_REQUEST_LEN) {
                    sendA2SChallenge(ctx, packet);
                    return;
                } else if (pckLength == A2S_INFO_REQUEST_LEN + LEN_CODE) { // 4 Byte padded challenge Code
                    sendA2SInfoResponse(ctx, packet);
                    return;
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Dropping Packet of Length {} bytes from {}:{}", pckLength,
                    packet.sender().getAddress().getHostAddress(), packet.sender().getPort());
        }
    }

    private void sendA2SChallenge(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        byte[] challenge = Cache.CHALLENGE_MAP.computeIfAbsent(new Cache.ByteKey(datagramPacket.sender().getAddress().getAddress()), key -> {
            // Generate Random Data of 4 Bytes only if there is no challenge code for this IP
            byte[] challengeCode = new byte[LEN_CODE];
            RANDOM.nextBytes(challengeCode);
            return challengeCode;
        });

        // Send A2S CHALLENGE Packet
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(A2S_CHALLENGE_RESPONSE_HEADER);
        byteBuf.writeBytes(challenge);
        ctx.writeAndFlush(new DatagramPacket(byteBuf, datagramPacket.sender()), ctx.voidPromise());
    }

    private void sendA2SPlayerResponse(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        if (isIPValid(datagramPacket, Arrays.copyOfRange(ByteBufUtil.getBytes(datagramPacket.content()),
                A2S_PLAYER_CODE_POS, A2S_PLAYER_CODE_POS + LEN_CODE), "A2S_PLAYER")) {
            ctx.writeAndFlush(new DatagramPacket(Cache.A2S_PLAYER, datagramPacket.sender()), ctx.voidPromise());
        }
    }

    private void sendA2SRulesResponse(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        if (isIPValid(datagramPacket, Arrays.copyOfRange(ByteBufUtil.getBytes(datagramPacket.content()),
                A2S_RULES_CODE_POS, A2S_RULES_CODE_POS + LEN_CODE), "A2S_RULES")) {
            ctx.writeAndFlush(new DatagramPacket(Cache.A2S_RULES, datagramPacket.sender()), ctx.voidPromise());
        }
    }

    private void sendA2SInfoResponse(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        if (isIPValid(datagramPacket, Arrays.copyOfRange(ByteBufUtil.getBytes(datagramPacket.content()),
                A2S_INFO_CODE_POS, A2S_INFO_CODE_POS + LEN_CODE), "A2S_INFO")) {
            ctx.writeAndFlush(new DatagramPacket(Cache.A2S_INFO, datagramPacket.sender()), ctx.voidPromise());
        }
    }

    private boolean isIPValid(DatagramPacket datagramPacket, byte[] challengeCode, String logTrace) {
        // Look for Client IP Address in Cache and load Challenge Code Value from it.
        // Some services reuse the same challenge code to retrieve all three packet types.
        // We want that as it helps minimize traffic in the internet. Hence, we only get() the code, not remove() it here.
        byte[] storedChallengeCode = Cache.CHALLENGE_MAP.get(new Cache.ByteKey(datagramPacket.sender().getAddress().getAddress()));

        // If Cache Value is not NULL it means we found the IP, and now we'll validate it.
        if (storedChallengeCode != null) {
            // Match received challenge code against Cache Stored challenge code
            if (Arrays.equals(storedChallengeCode, challengeCode)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Valid Challenge Code ({}) received from {}:{} [{}][REQUEST ACCEPTED]", ByteBufUtil.hexDump(challengeCode),
                            datagramPacket.sender().getAddress().getHostAddress(), datagramPacket.sender().getPort(), logTrace);
                }
                return true;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid Challenge Code ({}) received from {}:{} Expected Code: {} [{}][REQUEST DROPPED]", ByteBufUtil.hexDump(challengeCode),
                            datagramPacket.sender().getAddress().getHostAddress(), datagramPacket.sender().getPort(), storedChallengeCode, logTrace);
                }
                return false;
            }
        } else {
            if (logger.isDebugEnabled()) {
                // If you see lots of messages like this in the log, try raising the ChallengeCodeTTL (best practise is 2000)
                logger.debug("Unknown (Old?) Challenge Code ({}) received from {}:{} [{}][REQUEST DROPPED]", ByteBufUtil.hexDump(challengeCode),
                        datagramPacket.sender().getAddress().getHostAddress(), datagramPacket.sender().getPort(), logTrace);
            }
            return false;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Caught Error", cause);
    }
}
