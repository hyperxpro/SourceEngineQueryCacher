package com.aayushatharva.sourcecenginequerycacher.utils;

public final class Utils {

    /**
     * Convert Byte Array into Hex String
     *
     * @param bytes Byte Array
     * @return Hex String
     */
    public static String toHexString(final byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = "0123456789ABCDEF".toCharArray()[v >>> 4];
            hexChars[j * 2 + 1] = "0123456789ABCDEF".toCharArray()[v & 0x0F];
        }
        return new String(hexChars);
    }

    private Utils() {
        // Prevent outside initialization
    }
}
