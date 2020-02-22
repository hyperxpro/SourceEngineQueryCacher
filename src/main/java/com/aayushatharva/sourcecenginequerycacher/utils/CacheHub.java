package com.aayushatharva.sourcecenginequerycacher.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;

public final class CacheHub {
    public static byte[] A2S_INFO;
    public static byte[] A2S_PLAYER;

    public static final Cache<String, String> CHALLENGE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(1000000L)
            .expireAfterAccess(Duration.ofSeconds(5))
            .expireAfterWrite(Duration.ofSeconds(5))
            .build();
}
