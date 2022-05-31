package com.aayushatharva.seqc.utils;

import io.netty5.buffer.api.Buffer;
import io.netty5.buffer.api.BufferAllocator;

public final class Packets {

    // Length of Challenge Code
    public static final int LEN_CODE = 4;

    /**
     * FFFFFFFF41
     */
    public static final Buffer A2S_CHALLENGE_RESPONSE_HEADER = BufferAllocator.offHeapPooled()
            .copyOf(new byte[]{-1, -1, -1, -1, 65}).makeReadOnly();

    public static final int A2S_CHALLENGE_RESPONSE_HEADER_LEN = 5;
    public static final int A2S_CHALLENGE_RESPONSE_CODE_POS = 5;

    /**
     * FFFFFFFF54536F7572636520456E67696E6520517565727900
     * this packet is special as it will have 4 bytes of additional payload (padded to the end) when replying with a challenge code
     */
    public static final Buffer A2S_INFO_REQUEST = BufferAllocator.offHeapPooled()
            .copyOf(new byte[]{-1, -1, -1, -1, 84, 83, 111,
                    117, 114, 99, 101, 32, 69, 110, 103, 105, 110, 101,
                    32, 81, 117, 101, 114, 121, 0}).makeReadOnly();

    public static final int A2S_INFO_REQUEST_LEN = 25;
    public static final int A2S_INFO_CODE_POS = 25;

    /**
     * FFFFFFFF49
     */
    public static final Buffer A2S_INFO_RESPONSE_HEADER = BufferAllocator.offHeapPooled()
            .copyOf(new byte[]{-1, -1, -1, -1, 73}).makeReadOnly();

    public static final int A2S_INFO_RESPONSE_HEADER_LEN = 5;

    /**
     * FFFFFFFE49
     */
    public static final Buffer A2S_INFO_RESPONSE_HEADER_SPLIT = BufferAllocator.offHeapPooled()
            .copyOf(new byte[]{-1, -1, -1, -2, 73}).makeReadOnly();

    public static final int A2S_INFO_RESPONSE_HEADER_SPLIT_LEN = 5;


    //A2S_RULES
    /**
     * FFFFFFFF5600000000
     */
    public static final Buffer A2S_RULES_CHALLENGE_REQUEST_1 = BufferAllocator.offHeapPooled()
            .copyOf(new byte[]{-1, -1, -1, -1, 86, 0, 0, 0, 0}).makeReadOnly();

    public static final int A2S_RULES_CHALLENGE_REQUEST_1_LEN = 9;
    /**
     * FFFFFFFF56FFFFFFFF
     */
    public static final Buffer A2S_RULES_CHALLENGE_REQUEST_2 = BufferAllocator.offHeapPooled()
            .copyOf(new byte[]{-1, -1, -1, -1, 86, -1, -1, -1, -1}).makeReadOnly();

    public static final int A2S_RULES_CHALLENGE_REQUEST_2_LEN = 9;

    /**
     * FFFFFFFF56
     */
    public static final Buffer A2S_RULES_REQUEST_HEADER = BufferAllocator.offHeapPooled()
            .copyOf(new byte[]{-1, -1, -1, -1, 86}).makeReadOnly();

    public static final int A2S_RULES_REQUEST_HEADER_LEN = 5;
    public static final int A2S_RULES_CODE_POS = 5;

    /**
     * FFFFFFFF45
     */
    public static final Buffer A2S_RULES_RESPONSE_HEADER = BufferAllocator.offHeapPooled()
            .copyOf(new byte[]{-1, -1, -1, -1, 69}).makeReadOnly();

    public static final int A2S_RULES_RESPONSE_HEADER_LEN = 5;

    /**
     * FFFFFFFE45
     */
    public static final Buffer A2S_RULES_RESPONSE_HEADER_SPLIT = BufferAllocator.offHeapPooled()
            .copyOf(new byte[]{-1, -1, -1, -2, 69}).makeReadOnly();

    public static final int A2S_RULES_RESPONSE_HEADER_SPLIT_LEN = 5;


    //A2S_PLAYER
    /**
     * FFFFFFFF5500000000
     */
    public static final Buffer A2S_PLAYER_CHALLENGE_REQUEST_1 = BufferAllocator.offHeapPooled()
            .copyOf(new byte[]{-1, -1, -1, -1, 85, 0, 0, 0, 0}).makeReadOnly();

    public static final int A2S_PLAYER_CHALLENGE_REQUEST_1_LEN = 9;

    /**
     * FFFFFFFF55FFFFFFFF
     */
    public static final Buffer A2S_PLAYER_CHALLENGE_REQUEST_2 = BufferAllocator.offHeapPooled()
            .copyOf(new byte[]{-1, -1, -1, -1, 85, -1, -1, -1, -1}).makeReadOnly();

    public static final int A2S_PLAYER_CHALLENGE_REQUEST_2_LEN = 9;

    /**
     * FFFFFFFF55
     */
    public static final Buffer A2S_PLAYER_REQUEST_HEADER = BufferAllocator.offHeapPooled()
            .copyOf(new byte[]{-1, -1, -1, -1, 85}).makeReadOnly();

    public static final int A2S_PLAYER_REQUEST_HEADER_LEN = 5;
    public static final int A2S_PLAYER_CODE_POS = 5;

    /**
     * FFFFFFFF44
     */
    public static final Buffer A2S_PLAYER_RESPONSE_HEADER = BufferAllocator.offHeapPooled()
            .copyOf(new byte[]{-1, -1, -1, -1, 68}).makeReadOnly();

    public static final int A2S_PLAYER_RESPONSE_HEADER_LEN = 5;

    /**
     * FFFFFFFE44
     */
    public static final Buffer A2S_PLAYER_RESPONSE_HEADER_SPLIT = BufferAllocator.offHeapPooled()
            .copyOf(new byte[]{-1, -1, -1, -2, 68}).makeReadOnly();

    public static final int A2S_PLAYER_RESPONSE_HEADER_SPLIT_LEN = 5;

    private Packets() {
        // Prevent outside initialization
    }
}
