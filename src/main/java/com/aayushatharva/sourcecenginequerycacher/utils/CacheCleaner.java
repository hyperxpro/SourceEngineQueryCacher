package com.aayushatharva.sourcecenginequerycacher.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CacheCleaner extends Thread {

    private static final Logger logger = LogManager.getLogger(CacheCleaner.class);

    @Override
    public void run() {

        logger.atInfo().log("Starting Challenge Code Cache Cleaner");

        while (true) {
            // Cleanup expired Challenge Codes
            CacheHub.CHALLENGE_CACHE.cleanUp();

            try {
                // Wait before re-cleaning
                sleep(Config.ChallengeCodeCacheCleanerInterval);
            } catch (InterruptedException e) {
                logger.atError().withThrowable(e).log("Error at CacheCleaner During Interval Sleep");
                break;
            }
        }
    }
}
