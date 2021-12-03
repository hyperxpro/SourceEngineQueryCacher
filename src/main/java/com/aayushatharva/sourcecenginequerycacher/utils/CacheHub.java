package com.aayushatharva.sourcecenginequerycacher.utils;

import com.shieldblaze.expressgateway.common.map.SelfExpiringMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CacheHub {

    /**
     * <p> ByteBuf for `A2S_INFO` Packet. </p>
     */
    public static final ByteBuf A2S_INFO = PooledByteBufAllocator.DEFAULT.buffer();

    /**
     * <p> ByteBuf for `A2S_PLAYER` Packet. </p>
     */
    public static final ByteBuf A2S_PLAYER = PooledByteBufAllocator.DEFAULT.buffer();

    /**
     * <p> ByteBuf for `A2S_RULES` Packet. </p>
     */
    public static final ByteBuf A2S_RULES = PooledByteBufAllocator.DEFAULT.buffer();

    /**
     * Challenge Code Map
     * <p>
     * Key: IPv4 Byte array
     * Value: Challenge code Byte array
     */
    public static final Map<ByteKey, byte[]> CHALLENGE_MAP = new SelfExpiringMap<>(
            new ConcurrentHashMap<>(), Duration.ofMillis(Config.ChallengeCodeTTL), false
    );

    public static final class ByteKey {
      /**
       * Wrapper for IP Address Bytes (rightmost 2 bytes)
       *
       * This is a VERY specific wrapper class for a very specific use case
       * with memory footprint and speed of comparison in mind.
       * There are no NullPointer checks, no Class equality checks, no length checks,
       * no safe fallbacks. Consider yourself warned!
       *
       * Java stores Object IDs in 8 bytes and always pads to a value divisible by 8
       * (ObjectID + byte + padding == 16 == ObjectID + int + padding)
       * So in order to save conversion time in
       * equals() and hashCode() methods, we just use an int for storing our value.
       */
      private final int value;
      public ByteKey(byte[] array) {
        int i = array.length;
        int result = 0;
        //Only use the rightmost Bytes for storing. If array.length < 2 then we die.
        while (--i >= array.length - 2)
          result = 31 * result + array[i];

        this.value = result;
      }
      public int getValue() {
        return this.value;
      }
      public boolean equals(ByteKey bk) {
        /**
         * This is very unsafe if used with any other object.
         * Make sure to only ever use this in an environment where
         * you can make sure that you compare only to other ByteKeys
         */
        return this.value == bk.getValue();
      }
      public int hashCode() {
        //if hashCode is 0 then it will get handled as if it was Null
        return this.value;
      }
    }

  }
