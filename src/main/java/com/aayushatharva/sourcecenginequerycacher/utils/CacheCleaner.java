package com.aayushatharva.sourcecenginequerycacher.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CacheCleaner extends Thread {

    private static final Logger logger = LogManager.getLogger(CacheCleaner.class);
    private boolean keepRunning = true;

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {

        logger.atInfo().log("Starting Challenge Code Cache Cleaner");

        while (keepRunning) {
            // Cleanup expired Challenge Codes
            CacheHub.CHALLENGE_CACHE.cleanUp();

            try {
                // Wait before re-cleaning
                sleep(Config.ChallengeCodeCacheCleanerInterval);
            } catch (InterruptedException e) {
                logger.atError().withThrowable(e).log("Error at CacheCleaner During Sleep Interval");
                break;
            }

            // If false then we're requested to shutdown.
            if (!keepRunning) {
                return;
            }
        }
    }

    public void shutdown() {
        this.interrupt();
        keepRunning = false;
    }
}
