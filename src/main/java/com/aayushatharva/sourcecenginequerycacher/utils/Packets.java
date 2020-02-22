package com.aayushatharva.sourcecenginequerycacher.utils;

public final class Packets {
    /**
     * FFFFFFFF41
     */
    public static final byte[] A2S_PLAYER_CHALLENGE_RESPONSE = new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 65};

    /**
     * FFFFFFFF54536F7572636520456E67696E6520517565727900
     */
    public static final byte[] A2S_INFO_REQUEST = new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 84, (byte) 83, (byte) 111, (byte) 117, (byte) 114, (byte) 99, (byte) 101, (byte) 32, (byte) 69, (byte) 110, (byte) 103, (byte) 105, (byte) 110, (byte) 101, (byte) 32, (byte) 81, (byte) 117, (byte) 101, (byte) 114, (byte) 121, (byte) 0};

    /**
     * FFFFFFFF5500000000
     */
    public static final byte[] A2S_PLAYER_CHALLENGE_REQUEST_A = new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0};

    /**
     * FFFFFFFF55FFFFFFFF
     */
    public static final byte[] A2S_PLAYER_CHALLENGE_REQUEST_B = new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 85, (byte) -1, (byte) -1, (byte) -1, (byte) -1};

    /**
     * FFFFFFFF55
     */
    public static final byte[] A2S_PLAYER_HEADER = new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 85};
}
