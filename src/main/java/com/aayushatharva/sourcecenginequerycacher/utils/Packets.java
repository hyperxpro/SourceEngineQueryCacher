package com.aayushatharva.sourcecenginequerycacher.utils;

import com.aayushatharva.sourcecenginequerycacher.Main;
import io.netty.buffer.ByteBuf;

public final class Packets {
    //Length of Challenge Code
    public static final int LEN_CODE = 4;

                //A2S_INFO
    /**
     * FFFFFFFF41
     */
    public static final ByteBuf A2S_CHALLENGE_RESPONSE = Main.BYTE_BUF_ALLOCATOR.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 65});

    /**
     * FFFFFFFF54536F7572636520456E67696E6520517565727900
     * this packet is special as it will have 4 bytes of additional payload (padded to the end) when replying with a challenge code
     */
    public static final ByteBuf A2S_INFO_REQUEST = Main.BYTE_BUF_ALLOCATOR.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 84, 83, 111,
                    117, 114, 99, 101, 32, 69, 110, 103, 105, 110, 101,
                    32, 81, 117, 101, 114, 121, 0});

    /**
     * FFFFFFFF49
     */
    public static final ByteBuf A2S_INFO_RESPONSE_HEADER = Main.BYTE_BUF_ALLOCATOR.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 73});


    public static final int A2S_INFO_CODE_POS = 25;


                  //A2S_RULES
    /**
     * FFFFFFFF5600000000
     */
    public static final ByteBuf A2S_RULES_CHALLENGE_REQUEST_1 = Main.BYTE_BUF_ALLOCATOR.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 86, 0, 0, 0, 0 });

    /**
     * FFFFFFFF56FFFFFFFF
     */
    public static final ByteBuf A2S_RULES_CHALLENGE_REQUEST_2 = Main.BYTE_BUF_ALLOCATOR.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 86, -1, -1, -1, -1 });

    /**
     * FFFFFFFF56
     */
    public static final ByteBuf A2S_RULES_REQUEST_HEADER = Main.BYTE_BUF_ALLOCATOR.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 86});

    /**
     * FFFFFFFF45
     */
    public static final ByteBuf A2S_RULES_RESPONSE_HEADER = Main.BYTE_BUF_ALLOCATOR.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 69});


    public static final int A2S_RULES_CODE_POS = 5;


                //A2S_PLAYER
    /**
     * FFFFFFFF5500000000
     */
    public static final ByteBuf A2S_PLAYER_CHALLENGE_REQUEST_1 = Main.BYTE_BUF_ALLOCATOR.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 85, 0, 0, 0, 0});

    /**
     * FFFFFFFF55FFFFFFFF
     */
    public static final ByteBuf A2S_PLAYER_CHALLENGE_REQUEST_2 = Main.BYTE_BUF_ALLOCATOR.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 85, -1, -1, -1, -1});

    /**
     * FFFFFFFF55
     */
    public static final ByteBuf A2S_PLAYER_REQUEST_HEADER = Main.BYTE_BUF_ALLOCATOR.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 85});

    /**
     * FFFFFFFF44
     */
    public static final ByteBuf A2S_PLAYER_RESPONSE_HEADER = Main.BYTE_BUF_ALLOCATOR.directBuffer()
            .writeBytes(new byte[]{-1, -1, -1, -1, 68});


    public static final int A2S_PLAYER_CODE_POS = 5;
}
