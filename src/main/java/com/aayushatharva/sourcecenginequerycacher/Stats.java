package com.aayushatharva.sourcecenginequerycacher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class Stats extends Thread {
    public static final AtomicLong BPS = new AtomicLong();

    public static final AtomicLong PPS = new AtomicLong();
    private static final Logger logger = LogManager.getLogger(Stats.class);

    @Override
    public void run() {

        while (true) {

            if (Config.Stats_PPS && Config.Stats_BPS) {
                System.out.print("[" + getTimestamp() + "] [STATS] PPS: " + PPS.getAndSet(0L));
                System.out.println(" | BPS: " + calculateBps());
            } else {
                if (Config.Stats_PPS) {
                    System.out.println("[" + getTimestamp() + "] [STATS] PPS: " + PPS.getAndSet(0L) + " | BPS: 0");
                }

                if (Config.Stats_BPS) {
                    System.out.println("[" + getTimestamp() + "] [STATS] PPS: 0 | BPS: " + calculateBps());
                }
            }

            try {
                sleep(1000L);
            } catch (InterruptedException e) {
                logger.atError().withThrowable(e).log("Error at Stats 1 Second Interval Sleep");
                break;
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
}
