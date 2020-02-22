package com.aayushatharva.sourcecenginequerycacher.utils;

public class CacheCleaner extends Thread {

    @Override
    public void run() {

        while (true) {

            // Cleanup expired Cache
            CacheHub.CHALLENGE_CACHE.cleanUp();


            try {
                Thread.sleep(250L); // Wait for 250 Milliseconds before re-cleaning
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
