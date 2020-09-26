package com.aayushatharva.sourcecenginequerycacher.utils;

import com.aayushatharva.sourcecenginequerycacher.Main;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.ByteBuf;

import java.time.Duration;

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
     * Challenge Code Cache
     */
    public static final Cache<String, String> CHALLENGE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(Config.MaxChallengeCode)
            .expireAfterWrite(Duration.ofMillis(Config.ChallengeCodeTTL))
            .concurrencyLevel(Config.ChallengeCodeCacheConcurrency)
            .build();
}
