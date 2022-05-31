package com.aayushatharva.seqc.utils;

import com.shieldblaze.expressgateway.common.map.SelfExpiringMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.time.Duration;
import java.util.Map;

public final class Cache {

    /**
     * Challenge Code Map
     * <p>
     * Key: IPv4 Byte array
     * Value: Challenge code Byte array
     */
    public static final Map<ByteKey, Integer> CHALLENGE_MAP = new SelfExpiringMap<>(
            new Object2IntOpenHashMap<>(), Duration.ofMillis(Configuration.CHALLENGE_TTL), false
    );

    public static final class ByteKey {

        /**
         * Wrapper for IP Address Bytes (rightmost 2 bytes)
         * <p>
         * This is a VERY specific wrapper class for a very specific use case
         * with memory footprint and speed of comparison and map traversal in mind.
         * There are no NullPointer checks, no Class equality checks, no length checks,
         * no safe fallbacks. Consider yourself warned!
         * <p>
         * Java stores Object IDs in 8 bytes and always pads to a value divisible by 8
         * (ObjectID + byte + padding == 16 == ObjectID + int + padding)
         * So in order to save conversion time in
         * equals() and hashCode() methods, we just use an int for storing our value.
         */
        private final int value;

        public ByteKey(byte[] array) {
            int i = array.length - 1;
            //Only use the rightmost 2 Bytes for storing. If array.length < 2 then we die.
            this.value = (array[i - 1] & 0xFF) << 8 | (array[i] & 0xFF);
        }

        @Override
        public boolean equals(Object o) {
            /*
             * This is very unsafe if used with any other object.
             * Make sure to only ever use this in an environment where
             * you can make sure that you compare only to other ByteKeys
             */
            return this.value == ((ByteKey) o).value;
        }

        @Override
        public int hashCode() {
            //if hashCode is 0 then it will get handled as if it was Null
            return this.value;
        }
    }
}
