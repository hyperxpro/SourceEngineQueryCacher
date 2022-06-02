package com.aayushatharva.seqc.utils;

import io.netty5.buffer.api.Buffer;

public final class ExtraBufferUtil {

    public static boolean contains(Buffer data, Buffer toMatchWith) {
        if (data == null && toMatchWith != null || toMatchWith == null && data != null) {
            return false;
        }
        if (data == toMatchWith) {
            return true;
        }
        return equals(data, data.readerOffset(), toMatchWith, toMatchWith.readerOffset(), data.readableBytes());
    }

    public static boolean equals(Buffer data, int dataStartIndex, Buffer toMatchWith, int toMatchWithStartIndex, int length) {
        if (data.writerOffset() - length < dataStartIndex || toMatchWith.writerOffset() - length < toMatchWithStartIndex) {
            return false;
        }
        return equalsInner(data, dataStartIndex, toMatchWith, toMatchWithStartIndex, length);
    }

    private static boolean equalsInner(Buffer smallBuffer, int dataStartIndex, Buffer toMatchWith, int toMatchWithStartIndex, int length) {
        final int longCount = length >>> 3;
        final int byteCount = length & 7;

        for (int i = longCount; i > 0; i --) {
            if (smallBuffer.getLong(dataStartIndex) != toMatchWith.getLong(toMatchWithStartIndex)) {
                return false;
            }
            dataStartIndex += 8;
            toMatchWithStartIndex += 8;
        }

        for (int i = byteCount; i > 0; i --) {
            if (smallBuffer.getByte(dataStartIndex) != toMatchWith.getByte(toMatchWithStartIndex)) {
                return false;
            }
            dataStartIndex++;
            toMatchWithStartIndex++;
        }

        return true;
    }

    private ExtraBufferUtil() {
        // Prevent outside initialization
    }
}
