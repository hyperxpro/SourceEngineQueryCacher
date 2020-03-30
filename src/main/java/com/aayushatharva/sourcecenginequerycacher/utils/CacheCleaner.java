package com.aayushatharva.sourcecenginequerycacher.utils;

import com.aayushatharva.sourcecenginequerycacher.Config;

public class CacheCleaner extends Thread {

    @Override
    public void run() {

        while (true) {
            // Cleanup expired Challenge Codes
            CacheHub.CHALLENGE_CACHE.cleanUp();

            try {
                Thread.sleep(Config.ChallengeCodeCacheCleanerInterval); // Wait for 250 Milliseconds before re-cleaning
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
