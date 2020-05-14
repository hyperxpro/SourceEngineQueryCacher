package com.aayushatharva.sourcecenginequerycacher.utils;

import io.netty.buffer.ByteBuf;

public final class Utils {

    /**
     * Release {@link ByteBuf} safely
     * @param byteBuf {@link ByteBuf} to release
     */
    public static void safeRelease(ByteBuf byteBuf) {
        if (byteBuf != null && byteBuf.refCnt() > 0) {
            byteBuf.release();
        }
    }
}
