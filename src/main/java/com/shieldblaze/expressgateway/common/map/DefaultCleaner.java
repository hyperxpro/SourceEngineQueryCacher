package com.shieldblaze.expressgateway.common.map;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class DefaultCleaner extends Cleaner {

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledFuture<?> scheduledFuture;

    public DefaultCleaner(SelfExpiringMap selfExpiringMap) {
        super(selfExpiringMap);
        scheduledFuture = executorService.scheduleWithFixedDelay(this, 10, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() throws IOException {
        scheduledFuture.cancel(true);
        executorService.shutdown();
        super.close();
    }
}
