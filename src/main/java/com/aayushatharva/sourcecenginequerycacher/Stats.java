package com.aayushatharva.sourcecenginequerycacher;

import com.aayushatharva.sourcecenginequerycacher.utils.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class Stats extends Thread {

    private static final Logger logger = LogManager.getLogger(Stats.class);

    public static final AtomicLong BPS = new AtomicLong();
    public static final AtomicLong PPS = new AtomicLong();

    @Override
    public void run() {

        logger.atInfo().log("Starting Stats, PPS Enabled: " + Config.Stats_PPS + ", bPS Enabled: " + Config.Stats_bPS);

        while (true) {

            if (Config.Stats_PPS && Config.Stats_bPS) {
                System.out.print("[" + getTimestamp() + "] [STATS] PPS: " + PPS.getAndSet(0L));
                System.out.println(" | bPS: " + calculateBps());
            } else {
                if (Config.Stats_PPS) {
                    System.out.println("[" + getTimestamp() + "] [STATS] PPS: " + PPS.getAndSet(0L) + " | bPS: 0");
                }

                if (Config.Stats_bPS) {
                    System.out.println("[" + getTimestamp() + "] [STATS] PPS: 0 | bPS: " + calculateBps());
                }
            }

            try {
                sleep(1000L);
            } catch (InterruptedException e) {
                logger.atError().withThrowable(e).log("Error at Stats During Interval Sleep");
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
