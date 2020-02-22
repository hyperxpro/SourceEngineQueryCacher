package com.aayushatharva.sourcecenginequerycacher;

import java.util.concurrent.atomic.AtomicLong;

public class Stats extends Thread {
    public static final AtomicLong PPS = new AtomicLong();

    @Override
    public void run() {
        while (true) {
            System.out.println("Current Packet Per Second: " + PPS.get());
            PPS.set(0L);

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
