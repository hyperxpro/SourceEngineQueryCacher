package com.aayushatharva.sourcecenginequerycacher;

import com.aayushatharva.sourcecenginequerycacher.utils.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

final class Stats extends Thread {

    private static final Logger logger = LogManager.getLogger(Stats.class);
    private boolean keepRunning = true;

    public static final AtomicLong BPS = new AtomicLong();
    public static final AtomicLong PPS = new AtomicLong();

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {

        logger.info("Starting Stats, PPS Enabled: " + Config.Stats_PPS + ", bPS Enabled: " + Config.Stats_bPS);

        while (keepRunning) {

            String timestamp = getTimestamp();

            if (Config.Stats_PPS && Config.Stats_bPS) {
                System.out.print("[" + timestamp + "] [STATS] p/s: " + PPS.getAndSet(0L));
                System.out.println(" | b/s: " + calculateBps());
            } else {
                if (Config.Stats_PPS) {
                    System.out.println("[" + timestamp + "] [STATS] p/s: " + PPS.getAndSet(0L) + " | b/s: 0");
                }

                if (Config.Stats_bPS) {
                    System.out.println("[" + timestamp + "] [STATS] p/s: 0 | b/s: " + calculateBps());
                }
            }

            try {
                sleep(1000L);
            } catch (InterruptedException e) {
                logger.error("Error at Stats During Interval Sleep", e);
                return;
            }

            // If false then we're requested to shutdown.
            if (!keepRunning) {
                return;
            }
        }
    }

    @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
    private String calculateBps() {
        BigDecimal bits = new BigDecimal(BPS.getAndSet(0L));
        bits = bits.divide(new BigDecimal("8"));
        return bits.toString();
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        return sdf.format(new Date());
    }

    public void shutdown() {
        interrupt();
        keepRunning = false;
    }
}
