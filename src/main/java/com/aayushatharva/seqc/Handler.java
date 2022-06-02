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
package com.aayushatharva.seqc;

import com.aayushatharva.seqc.utils.Cache;
import com.aayushatharva.seqc.utils.Configuration;
import com.aayushatharva.seqc.utils.ExtraBufferUtil;
import io.netty5.buffer.BufferUtil;
import io.netty5.buffer.api.Buffer;
import io.netty5.channel.ChannelHandler;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.channel.SimpleChannelInboundHandler;
import io.netty5.channel.socket.DatagramPacket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.SplittableRandom;

import static com.aayushatharva.seqc.utils.Packets.A2S_CHALLENGE_RESPONSE_HEADER;
import static com.aayushatharva.seqc.utils.Packets.A2S_CHALLENGE_RESPONSE_HEADER_LEN;
import static com.aayushatharva.seqc.utils.Packets.A2S_INFO_REQUEST;
import static com.aayushatharva.seqc.utils.Packets.A2S_INFO_REQUEST_LEN;
import static com.aayushatharva.seqc.utils.Packets.A2S_PLAYER_CHALLENGE_REQUEST_1;
import static com.aayushatharva.seqc.utils.Packets.A2S_PLAYER_CHALLENGE_REQUEST_2;
import static com.aayushatharva.seqc.utils.Packets.A2S_PLAYER_REQUEST_HEADER;
import static com.aayushatharva.seqc.utils.Packets.A2S_RULES_CHALLENGE_REQUEST_1;
import static com.aayushatharva.seqc.utils.Packets.A2S_RULES_CHALLENGE_REQUEST_2;
import static com.aayushatharva.seqc.utils.Packets.A2S_RULES_REQUEST_HEADER;
import static com.aayushatharva.seqc.utils.Packets.LEN_CODE;

@ChannelHandler.Sharable
public final class Handler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(Handler.class);
    public static final Handler INSTANCE = new Handler();
    private static final SplittableRandom RANDOM = new SplittableRandom();
    private static final boolean IS_LOGGER_DEBUG_ENABLED = logger.isDebugEnabled();

    // Buffer for `A2S_INFO` Packet
    public final List<Buffer> A2S_INFO = new ObjectArrayList<>();

    // Buffer for `A2S_PLAYER` Packet
    public final List<Buffer> A2S_PLAYER = new ObjectArrayList<>();

    // Buffer for `A2S_RULES` Packet
    public final List<Buffer> A2S_RULES = new ObjectArrayList<>();

    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket packet) {
        Buffer buffer = packet.content();
        int pckLength = packet.content().readableBytes();

        if (Configuration.STATS_PPS)
            Stats.PPS.incrementAndGet();

        if (Configuration.STATS_BPS)
            Stats.BPS.addAndGet(pckLength);

        /*
         * Packet size of 25, 29 bytes and 9 bytes only will be processed rest will be dropped.
         *
         * A2S_INFO = 25 Bytes, 29 bytes with padded challenge code
         * A2S_Player = 9 Bytes
         * A2S_RULES = 9 Bytes
         */
        if (pckLength == 9 || pckLength == 25 || pckLength == 29) {

            if ((pckLength == 25 || pckLength == 29) && ExtraBufferUtil.contains(A2S_INFO_REQUEST, buffer)) {
                /*
                 * 1. Packet equals to `A2S_INFO_REQUEST` with length==25 (A2S_INFO without challenge code)
                 * then we'll check if A2SInfoChallenge is enabled or not. If it's enabled then
                 *  we'll send response of A2S_Challenge Packet, otherwise we'll send A2S_INFO Packet.
                 *
                 * 2. Validate A2S_INFO Challenge Response (length==29) and send A2S_INFO Packet.
                 */
                if (pckLength == A2S_INFO_REQUEST_LEN) {
                    if (Configuration.ENABLE_A2S_INFO_CHALLENGE) {
                        sendA2SChallenge(ctx, packet);
                    } else {
                        sendA2SInfoResponse(ctx, packet, true);
                    }
                } else {
                    sendA2SInfoResponse(ctx, packet, false);
                }
                return;
            }

            if (Configuration.ENABLE_A2S_PLAYER && ExtraBufferUtil.contains(A2S_PLAYER_REQUEST_HEADER, buffer)) {
                /*
                 * 1. Packet equals to `A2S_PLAYER_CHALLENGE_REQUEST_1` or `A2S_PLAYER_CHALLENGE_REQUEST_2`
                 * then we'll send response of A2S_Player Challenge Packet.
                 */
                if (ExtraBufferUtil.contains(A2S_PLAYER_CHALLENGE_REQUEST_1, buffer) || ExtraBufferUtil.contains(A2S_PLAYER_CHALLENGE_REQUEST_2, buffer)) {
                    sendA2SChallenge(ctx, packet);
                } else {
                    // 2. Validate A2S_Player Challenge Response and send A2S_Player Packet.
                    sendA2SPlayerResponse(ctx, packet);
                }
                return;
            }

            if (Configuration.ENABLE_A2S_RULE && ExtraBufferUtil.contains(A2S_RULES_REQUEST_HEADER, buffer)) {
                /*
                 * 1. Packet equals `A2S_RULES_CHALLENGE_REQUEST_1` or `A2S_RULES_CHALLENGE_REQUEST_2`
                 * then we'll send response of A2S_Challenge Packet.
                 */
                if (ExtraBufferUtil.contains(A2S_RULES_CHALLENGE_REQUEST_1, buffer) || ExtraBufferUtil.contains(A2S_RULES_CHALLENGE_REQUEST_2, buffer)) {
                    sendA2SChallenge(ctx, packet);
                } else {
                    // 2. Validate A2S_RULES Challenge Response and send A2S_Rules Packet.
                    sendA2SRulesResponse(ctx, packet);
                }
                return;
            }
        }

        if (IS_LOGGER_DEBUG_ENABLED) {
            logger.debug("Dropping Packet of Length {} bytes from {}:{} ----- {}", pckLength,
                    packet.sender().getAddress().getHostAddress(), packet.sender().getPort(),
                    BufferUtil.hexDump(buffer));
        }
    }

    private void sendA2SChallenge(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        int challengeCode = Cache.CHALLENGE_MAP.computeIfAbsent(new Cache.ByteKey(datagramPacket.sender().getAddress().getAddress()), key -> {
            // Generate random Integer (32-bit) only if there is no challenge code for this IP address
            return RANDOM.nextInt();
        });

        // Send A2S CHALLENGE Packet
        Buffer buffer = ctx.bufferAllocator().allocate(A2S_CHALLENGE_RESPONSE_HEADER_LEN + LEN_CODE);
        buffer.writeBytes(A2S_CHALLENGE_RESPONSE_HEADER.copy());
        buffer.writeInt(challengeCode);
        ctx.writeAndFlush(new DatagramPacket(buffer, datagramPacket.sender()));
    }

    private void sendA2SInfoResponse(ChannelHandlerContext ctx, DatagramPacket datagramPacket, boolean direct) {
        // If 'direct' is 'true' then we will short-circuit and send A2S_INFO directly without challenge code validation.
        // If not then we will validate IP address and challenge code and upon successful validation, we will send A2S_INFO packet.
        if (direct || isIPValid(datagramPacket.sender(), datagramPacket, "A2S_INFO")) {
            if (A2S_INFO.size() == 1) {
                ctx.writeAndFlush(new DatagramPacket(A2S_INFO.get(0).copy(), datagramPacket.sender()));
                return;
            }
            for (int i = 0; i < A2S_INFO.size(); i++) {
                ctx.writeAndFlush(new DatagramPacket(A2S_INFO.get(i).copy(), datagramPacket.sender()));
            }
        }
    }

    private void sendA2SPlayerResponse(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        if (isIPValid(datagramPacket.sender(), datagramPacket, "A2S_PLAYER")) {
            if (A2S_PLAYER.size() == 1) {
                ctx.writeAndFlush(new DatagramPacket(A2S_PLAYER.get(0).copy(), datagramPacket.sender()));
                return;
            }
            for (int i = 0; i < A2S_PLAYER.size(); i++) {
                ctx.writeAndFlush(new DatagramPacket(A2S_PLAYER.get(i).copy(), datagramPacket.sender()));
            }
        }
    }

    private void sendA2SRulesResponse(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        if (isIPValid(datagramPacket.sender(), datagramPacket, "A2S_RULES")) {
            if (A2S_RULES.size() == 1) {
                ctx.writeAndFlush(new DatagramPacket(A2S_RULES.get(0).copy(), datagramPacket.sender()));
                return;
            }
            for (int i = 0; i < A2S_RULES.size(); i++) {
                ctx.writeAndFlush(new DatagramPacket(A2S_RULES.get(i).copy(), datagramPacket.sender()));
            }
        }
    }

    private boolean isIPValid(InetSocketAddress socketAddress, DatagramPacket packet, String logTrace) {
        // Look for Client IP Address in Cache and load Challenge Code Value from it.
        // Some services reuse the same challenge code to retrieve all three packet types.
        // We want that as it helps minimize traffic in the internet. Hence, we only get() the code, not remove() it here.
        int storedChallengeCode = Cache.CHALLENGE_MAP.getInt(new Cache.ByteKey(socketAddress.getAddress().getAddress()));
        int challengeCode = packet.content().readerOffset(5).readInt();

        // If Cache Value is not '0' (zero) it means we found the IP, and now we'll validate it.
        if (storedChallengeCode != 0) {
            // Match received challenge code against Cache Stored challenge code
            if (storedChallengeCode == challengeCode) {
                if (IS_LOGGER_DEBUG_ENABLED) {
                    logger.debug("Valid Challenge Code ({}) received from {}:{} [{}][REQUEST ACCEPTED]", challengeCode,
                            socketAddress.getAddress().getHostAddress(), socketAddress.getPort(), logTrace);
                }
                return true;
            } else {
                if (IS_LOGGER_DEBUG_ENABLED) {
                    logger.debug("Invalid Challenge Code ({}) received from {}:{} Expected Code: {} [{}][REQUEST DROPPED]", challengeCode,
                            socketAddress.getAddress().getHostAddress(), socketAddress.getPort(), storedChallengeCode, logTrace);
                }
                return false;
            }
        } else {
            if (IS_LOGGER_DEBUG_ENABLED) {
                // If you see lots of messages like this in the log, try raising the ChallengeCodeTTL (best practise is 2000)
                logger.debug("Unknown (Old?) Challenge Code ({}) received from {}:{} [{}][REQUEST DROPPED]", challengeCode,
                        socketAddress.getAddress().getHostAddress(), socketAddress.getPort(), logTrace);
            }
            return false;
        }
    }

    public synchronized void receiveA2sInfo(List<Buffer> buffers) {
        for (int i = 0; i < A2S_INFO.size(); i++) {
            A2S_INFO.get(i).close();
        }
        A2S_INFO.clear();
        A2S_INFO.addAll(buffers);
    }

    public synchronized void receiveA2sPlayer(List<Buffer> buffers) {
        for (int i = 0; i < A2S_PLAYER.size(); i++) {
            A2S_PLAYER.get(i).close();
        }
        A2S_PLAYER.clear();
        A2S_PLAYER.addAll(buffers);
    }

    public synchronized void receiveA2sRule(List<Buffer> buffers) {
        for (int i = 0; i < A2S_RULES.size(); i++) {
            A2S_RULES.get(i).close();
        }
        A2S_RULES.clear();
        A2S_RULES.addAll(buffers);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // NO-OP; STFU!
    }

    private Handler() {
        // Prevent outside initialization
    }
}
