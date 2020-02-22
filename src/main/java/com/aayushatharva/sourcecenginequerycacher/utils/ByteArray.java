package com.aayushatharva.sourcecenginequerycacher.utils;

import java.util.Arrays;

public final class ByteArray {

    /**
     * Join 2 Byte Arrays
     * @param array1 Byte Array 1
     * @param array2 Byte Array 2
     * @return Joined Byte Array
     */
    public static byte[] joinArrays(final byte[] array1, byte[] array2) {
        byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

}
