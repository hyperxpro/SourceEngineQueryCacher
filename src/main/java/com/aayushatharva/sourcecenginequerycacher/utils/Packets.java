package com.aayushatharva.sourcecenginequerycacher.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

public final class Packets {

    //Length of Challenge Code
    public static final int LEN_CODE = 4;

    /**
     * FFFFFFFF41
     */
    public static final ByteBuf A2S_CHALLENGE_RESPONSE_HEADER = Unpooled.unreleasableBuffer(Unpooled.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 65}));

    public static final int A2S_CHALLENGE_RESPONSE_HEADER_LEN = 5;
    public static final int A2S_CHALLENGE_RESPONSE_CODE_POS = 5;

    /**
     * FFFFFFFF54536F7572636520456E67696E6520517565727900
     * this packet is special as it will have 4 bytes of additional payload (padded to the end) when replying with a challenge code
     */
    public static final ByteBuf A2S_INFO_REQUEST = Unpooled.unreleasableBuffer(Unpooled.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 84, 83, 111,
                    117, 114, 99, 101, 32, 69, 110, 103, 105, 110, 101,
                    32, 81, 117, 101, 114, 121, 0}));

    public static final int A2S_INFO_REQUEST_LEN = 25;
    public static final int A2S_INFO_CODE_POS = 25;

    /**
     * FFFFFFFF49
     */
    public static final ByteBuf A2S_INFO_RESPONSE_HEADER = Unpooled.unreleasableBuffer(Unpooled.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 73}));

    public static final int A2S_INFO_RESPONSE_HEADER_LEN = 5;

    /**
     * FFFFFFFE49
     */
    public static final ByteBuf A2S_INFO_RESPONSE_HEADER_SPLIT = Unpooled.unreleasableBuffer(Unpooled.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -2, 73}));

    public static final int A2S_INFO_RESPONSE_HEADER_SPLIT_LEN = 5;


    //A2S_RULES
    /**
     * FFFFFFFF5600000000
     */
    public static final ByteBuf A2S_RULES_CHALLENGE_REQUEST_1 = Unpooled.unreleasableBuffer(Unpooled.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 86, 0, 0, 0, 0}));

    public static final int A2S_RULES_CHALLENGE_REQUEST_1_LEN = 9;
    /**
     * FFFFFFFF56FFFFFFFF
     */
    public static final ByteBuf A2S_RULES_CHALLENGE_REQUEST_2 = Unpooled.unreleasableBuffer(Unpooled.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 86, -1, -1, -1, -1}));

    public static final int A2S_RULES_CHALLENGE_REQUEST_2_LEN = 9;

    /**
     * FFFFFFFF56
     */
    public static final ByteBuf A2S_RULES_REQUEST_HEADER = Unpooled.unreleasableBuffer(Unpooled.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 86}));

    public static final int A2S_RULES_REQUEST_HEADER_LEN = 5;
    public static final int A2S_RULES_CODE_POS = 5;

    /**
     * FFFFFFFF45
     */
    public static final ByteBuf A2S_RULES_RESPONSE_HEADER = Unpooled.unreleasableBuffer(Unpooled.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 69}));

    public static final int A2S_RULES_RESPONSE_HEADER_LEN = 5;

    /**
     * FFFFFFFE45
     */
    public static final ByteBuf A2S_RULES_RESPONSE_HEADER_SPLIT = Unpooled.unreleasableBuffer(Unpooled.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -2, 69}));

    public static final int A2S_RULES_RESPONSE_HEADER_SPLIT_LEN = 5;


    //A2S_PLAYER
    /**
     * FFFFFFFF5500000000
     */
    public static final ByteBuf A2S_PLAYER_CHALLENGE_REQUEST_1 = Unpooled.unreleasableBuffer(Unpooled.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 85, 0, 0, 0, 0}));

    public static final int A2S_PLAYER_CHALLENGE_REQUEST_1_LEN = 9;

    /**
     * FFFFFFFF55FFFFFFFF
     */
    public static final ByteBuf A2S_PLAYER_CHALLENGE_REQUEST_2 = Unpooled.unreleasableBuffer(Unpooled.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 85, -1, -1, -1, -1}));

    public static final int A2S_PLAYER_CHALLENGE_REQUEST_2_LEN = 9;

    /**
     * FFFFFFFF55
     */
    public static final ByteBuf A2S_PLAYER_REQUEST_HEADER = Unpooled.unreleasableBuffer(Unpooled.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 85}));

    public static final int A2S_PLAYER_REQUEST_HEADER_LEN = 5;
    public static final int A2S_PLAYER_CODE_POS = 5;

    /**
     * FFFFFFFF44
     */
    public static final ByteBuf A2S_PLAYER_RESPONSE_HEADER = Unpooled.unreleasableBuffer(Unpooled.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 68}));

    public static final int A2S_PLAYER_RESPONSE_HEADER_LEN = 5;

    /**
     * FFFFFFFE44
     */
    public static final ByteBuf A2S_PLAYER_RESPONSE_HEADER_SPLIT = Unpooled.unreleasableBuffer(Unpooled.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -2, 68}));

    public static final int A2S_PLAYER_RESPONSE_HEADER_SPLIT_LEN = 5;
}
