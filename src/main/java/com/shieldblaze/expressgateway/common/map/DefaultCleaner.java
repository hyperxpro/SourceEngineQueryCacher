package com.shieldblaze.expressgateway.common.map;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @param <K> Key
 * @param <V> Value
 */
final class DefaultCleaner<K, V> extends Cleaner<K, V> {

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledFuture<?> scheduledFuture;

    DefaultCleaner(SelfExpiringMap<K, V> selfExpiringMap) {
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
