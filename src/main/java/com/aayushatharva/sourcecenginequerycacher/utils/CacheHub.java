package com.aayushatharva.sourcecenginequerycacher.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.ByteBuf;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public final class CacheHub {

    /**
     * <p> ByteBuf Holder for `A2S_INFO` Packet. </p>
     */
    public static final AtomicReference<ByteBuf> A2S_INFO = new AtomicReference<>();

    /**
     * <p> ByteBuf Holder for `A2S_PLAYER` Packet. </p>
     */
    public static final AtomicReference<ByteBuf> A2S_PLAYER = new AtomicReference<>();

    /**
     * Challenge Code Cache
     */
    public static final Cache<String, String> CHALLENGE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(Config.MaxChallengeCode)
            .expireAfterAccess(Duration.ofMillis(Config.ChallengeCodeTTL))
            .expireAfterWrite(Duration.ofSeconds(Config.ChallengeCodeTTL))
            .concurrencyLevel(Config.ChallengeCodeCacheConcurrency)
            .build();
}
