package com.aayushatharva.sourcecenginequerycacher;

import com.aayushatharva.sourcecenginequerycacher.utils.Cache;
import com.aayushatharva.sourcecenginequerycacher.utils.Config;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static com.aayushatharva.sourcecenginequerycacher.utils.Packets.A2S_CHALLENGE_RESPONSE_HEADER;
import static com.aayushatharva.sourcecenginequerycacher.utils.Packets.A2S_INFO_CODE_POS;
import static com.aayushatharva.sourcecenginequerycacher.utils.Packets.A2S_INFO_REQUEST;
import static com.aayushatharva.sourcecenginequerycacher.utils.Packets.A2S_INFO_REQUEST_LEN;
import static com.aayushatharva.sourcecenginequerycacher.utils.Packets.A2S_PLAYER_CHALLENGE_REQUEST_1;
import static com.aayushatharva.sourcecenginequerycacher.utils.Packets.A2S_PLAYER_CHALLENGE_REQUEST_2;
import static com.aayushatharva.sourcecenginequerycacher.utils.Packets.A2S_PLAYER_CODE_POS;
import static com.aayushatharva.sourcecenginequerycacher.utils.Packets.A2S_PLAYER_REQUEST_HEADER;
import static com.aayushatharva.sourcecenginequerycacher.utils.Packets.A2S_PLAYER_REQUEST_HEADER_LEN;
import static com.aayushatharva.sourcecenginequerycacher.utils.Packets.LEN_CODE;

@ChannelHandler.Sharable
final class Handler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(Handler.class);
    private static final boolean IS_LOGGER_DEBUG_ENABLED = logger.isDebugEnabled();
    private static final boolean IS_LOGGER_ERROR_ENABLED = logger.isErrorEnabled();

    private static final AtomicReference<byte[]> CHALLENGE_CACHED = new AtomicReference<>(new byte[4]);
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(2);

    static {
        new Thread(() -> {
            final SplittableRandom RANDOM = new SplittableRandom();

            while (true) {
                try {
                    byte[] bytes = new byte[4];
                    RANDOM.nextBytes(bytes);
                    CHALLENGE_CACHED.set(bytes);

                    // Refresh every 5 seconds
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    // This will never happen.
                }
            }
        }).start();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        final int pckLength = datagramPacket.content().readableBytes();
        final ByteBuf data = datagramPacket.content();

        if (Config.Stats_PPS) {
            EXECUTOR_SERVICE.submit(Stats.PPS::incrementAndGet);
        }

        if (Config.Stats_bPS) {
            EXECUTOR_SERVICE.submit(() -> Stats.BPS.addAndGet(pckLength));
        }

        /*
         * If A2S_INFO or A2S_PLAYER is not readable or If A2S_RULE is enabled but not readable,
         * drop request because we've nothing to reply.
         */
        if ((!Cache.A2S_INFO.isReadable() || !Cache.A2S_PLAYER.isReadable()) && IS_LOGGER_ERROR_ENABLED) {
            String stringBuilder = "------------ Dropping query request because Cache is not ready -----------" + "\r\n" +
                    "A2S_INFO: " + Cache.A2S_INFO + "\r\n" +
                    "A2S_PLAYER: " + Cache.A2S_PLAYER + "\r\n" +
                    "-----------------------";

            System.out.println(stringBuilder);
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

            if (ByteBufUtil.equals(A2S_PLAYER_REQUEST_HEADER, data.slice(0, A2S_PLAYER_REQUEST_HEADER_LEN))) {

                /*
                 * 1. Packet equals to `A2S_PLAYER_CHALLENGE_REQUEST_1` or `A2S_PLAYER_CHALLENGE_REQUEST_2`
                 * then we'll send response of A2S_Player Challenge Packet.
                 */
                if (ByteBufUtil.equals(data, A2S_PLAYER_CHALLENGE_REQUEST_2) || ByteBufUtil.equals(data, A2S_PLAYER_CHALLENGE_REQUEST_1)) {
                    sendA2SChallenge(ctx, datagramPacket);
                } else {
                    // 2. Validate A2S_Player Challenge Response and send A2S_Player Packet.
                    sendA2SPlayerResponse(ctx, datagramPacket);
                }
                return;
            } else if (ByteBufUtil.equals(A2S_INFO_REQUEST, data.slice(0, A2S_INFO_REQUEST_LEN))) {

                /*
                 * 1. Packet equals to `A2S_INFO_REQUEST` with length==25 (A2S_INFO without challenge code)
                 * then we'll send response of A2S_Challenge Packet.
                 *
                 * 2. Validate A2S_INFO Challenge Response (length==29) and send A2S_INFO Packet.
                 */
                if (pckLength == A2S_INFO_REQUEST_LEN) {
                    sendA2SChallenge(ctx, datagramPacket);
                    return;
                } else if (pckLength == A2S_INFO_REQUEST_LEN + LEN_CODE) { // 4 Byte padded challenge Code
                    sendA2SInfoResponse(ctx, datagramPacket);
                    return;
                }
            }
        }

        if (IS_LOGGER_DEBUG_ENABLED) {
            logger.debug("Dropping Packet of Length {} bytes from {}:{}", pckLength,
                    datagramPacket.sender().getAddress().getHostAddress(), datagramPacket.sender().getPort());
        }
    }

    private static void sendA2SChallenge(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        byte[] challenge = Cache.CHALLENGE_MAP.computeIfAbsent(new Cache.ByteKey(datagramPacket.sender().getAddress().getAddress()), key -> CHALLENGE_CACHED.get());

        // Send A2S CHALLENGE Packet
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(A2S_CHALLENGE_RESPONSE_HEADER.copy());
        byteBuf.writeBytes(challenge);
        ctx.writeAndFlush(new DatagramPacket(byteBuf, datagramPacket.sender()), ctx.voidPromise());
    }

    private static void sendA2SPlayerResponse(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        if (isIPValid(datagramPacket, Arrays.copyOfRange(ByteBufUtil.getBytes(datagramPacket.content()), A2S_PLAYER_CODE_POS, A2S_PLAYER_CODE_POS + LEN_CODE), "A2S_PLAYER")) {
            ctx.writeAndFlush(new DatagramPacket(Cache.A2S_PLAYER.copy(), datagramPacket.sender()), ctx.voidPromise());
        }
    }

    private static void sendA2SInfoResponse(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        if (isIPValid(datagramPacket, Arrays.copyOfRange(ByteBufUtil.getBytes(datagramPacket.content()), A2S_INFO_CODE_POS, A2S_INFO_CODE_POS + LEN_CODE), "A2S_INFO")) {
            ctx.writeAndFlush(new DatagramPacket(Cache.A2S_INFO.copy(), datagramPacket.sender()), ctx.voidPromise());
        }
    }

    private static boolean isIPValid(DatagramPacket datagramPacket, byte[] challengeCode, String logTrace) {
        // Look for Client IP Address in Cache and load Challenge Code Value from it.
        // Some services reuse the same challenge code to retrieve all three packet types.
        // We want that as it helps minimize traffic in the internet. Hence, we only get() the code, not remove() it here.
        byte[] storedChallengeCode = Cache.CHALLENGE_MAP.get(new Cache.ByteKey(datagramPacket.sender().getAddress().getAddress()));

        // If Cache Value is not NULL it means we found the IP, and now we'll validate it.
        if (storedChallengeCode != null) {
            // Match received challenge code against Cache Stored challenge code
            if (Arrays.equals(storedChallengeCode, challengeCode)) {
                if (IS_LOGGER_DEBUG_ENABLED) {
                    logger.debug("Valid Challenge Code ({}) received from {}:{} [{}][REQUEST ACCEPTED]", ByteBufUtil.hexDump(challengeCode),
                            datagramPacket.sender().getAddress().getHostAddress(), datagramPacket.sender().getPort(), logTrace);
                }
                return true;
            } else {
                if (IS_LOGGER_DEBUG_ENABLED) {
                    logger.debug("Invalid Challenge Code ({}) received from {}:{} Expected Code: {} [{}][REQUEST DROPPED]", ByteBufUtil.hexDump(challengeCode),
                            datagramPacket.sender().getAddress().getHostAddress(), datagramPacket.sender().getPort(), storedChallengeCode, logTrace);
                }
                return false;
            }
        } else {
            if (IS_LOGGER_DEBUG_ENABLED) {
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
