/*
 * This file is part of ShieldBlaze ExpressGateway. [www.shieldblaze.com]
 * Copyright (c) 2020-2021 ShieldBlaze
 *
 * ShieldBlaze ExpressGateway is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ShieldBlaze ExpressGateway is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ShieldBlaze ExpressGateway.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.shieldblaze.expressgateway.common.map;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * {@link SelfExpiringMap} automatically expires entries using a {@link Cleaner}.
 *
 * @param <K> Key
 * @param <V> Value
 */
public final class SelfExpiringMap<K, V> extends ExpiringMap<K, V> implements Closeable {

    private final Cleaner<K, V> cleaner;

    public SelfExpiringMap(Duration duration) {
        super(duration);
        cleaner = new DefaultCleaner<>(this);
    }

    public SelfExpiringMap(Duration duration, Cleaner<K, V> cleaner) {
        super(duration);
        this.cleaner = cleaner;
    }

    public SelfExpiringMap(Map<K, V> storageMap, Duration duration, boolean autoRenew) {
        super(storageMap, duration, autoRenew);
        cleaner = new DefaultCleaner<>(this);
    }

    public SelfExpiringMap(Map<K, V> storageMap, Duration duration, boolean autoRenew, Cleaner<K, V> cleaner) {
        super(storageMap, duration, autoRenew);
        this.cleaner = cleaner;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void close() throws IOException {
        cleaner.close();
    }
}
