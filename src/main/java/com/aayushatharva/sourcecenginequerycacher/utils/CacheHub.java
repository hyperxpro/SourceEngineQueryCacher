package com.aayushatharva.sourcecenginequerycacher.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public final class CacheHub {

    /**
     * <p> Byte Array Holder for `A2S_INFO` Packet. </p>
     */
    public static final AtomicReference<ByteBuffer> A2S_INFO = new AtomicReference<>();

    /**
     * <p> Byte Array Holder for `A2S_PLAYER` Packet. </p>
     */
    public static final AtomicReference<ByteBuffer> A2S_PLAYER = new AtomicReference<>();

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
