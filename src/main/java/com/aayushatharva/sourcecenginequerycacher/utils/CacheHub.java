package com.aayushatharva.sourcecenginequerycacher.utils;

import com.aayushatharva.sourcecenginequerycacher.Main;
import com.shieldblaze.expressgateway.common.map.SelfExpiringMap;
import io.netty.buffer.ByteBuf;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CacheHub {

    /**
     * <p> ByteBuf for `A2S_INFO` Packet. </p>
     */
    public static final ByteBuf A2S_INFO = Main.BYTE_BUF_ALLOCATOR.buffer();

    /**
     * <p> ByteBuf for `A2S_PLAYER` Packet. </p>
     */
    public static final ByteBuf A2S_PLAYER = Main.BYTE_BUF_ALLOCATOR.buffer();

    /**
     * <p> ByteBuf for `A2S_RULES` Packet. </p>
     */
    public static final ByteBuf A2S_RULES = Main.BYTE_BUF_ALLOCATOR.buffer();

    /**
     * Challenge Code Map
     */
    public static final Map<String, String> CHALLENGE_MAP = new SelfExpiringMap<>(new ConcurrentHashMap<>(),
            Duration.ofMillis(Config.ChallengeCodeTTL),
            false);
}
