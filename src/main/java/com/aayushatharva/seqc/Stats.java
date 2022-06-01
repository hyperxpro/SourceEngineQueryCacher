/*
 * This file is part of SourceEngineQueryCacher. [https://github.com/hyperxpro/SourceEngineQueryCacher]
 * Copyright (c) 2020-2022 Aayush Atharva
 *
 * SourceEngineQueryCacher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SourceEngineQueryCacher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SourceEngineQueryCacher.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.aayushatharva.seqc;

import com.aayushatharva.seqc.utils.Configuration;
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
        logger.info("Starting Stats, PPS Enabled: " + Configuration.STATS_PPS + ", bPS Enabled: " + Configuration.STATS_BPS);

        String timestamp;
        while (keepRunning) {

             timestamp = getTimestamp();

            if (Configuration.STATS_PPS && Configuration.STATS_BPS) {
                System.out.print("[" + timestamp + "] [STATS] p/s: " + PPS.getAndSet(0L));
                System.out.print(" | b/s: " + calculateBps());
                System.out.print("\r");
            } else {
                if (Configuration.STATS_PPS) {
                    System.out.print("[" + timestamp + "] [STATS] p/s: " + PPS.getAndSet(0L) + " | b/s: 0");
                    System.out.print("\r");
                }

                if (Configuration.STATS_BPS) {
                    System.out.print("[" + timestamp + "] [STATS] p/s: 0 | b/s: " + calculateBps());
                    System.out.print("\r");
                }
            }

            try {
                sleep(1000L);
            } catch (InterruptedException e) {
                logger.error("Sleep Interrupted");
                break;
            }

            // If false then we're requested to shut down.
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
        this.keepRunning = false;
        this.interrupt();
    }
}
