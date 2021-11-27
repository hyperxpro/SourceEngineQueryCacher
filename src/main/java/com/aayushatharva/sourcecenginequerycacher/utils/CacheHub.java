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
    public static final Map<byte[], byte[]> CHALLENGE_MAP = new SelfExpiringMap<>(
            new ConcurrentHashMap<>(), Duration.ofMillis(Config.ChallengeCodeTTL), false
    );
}
