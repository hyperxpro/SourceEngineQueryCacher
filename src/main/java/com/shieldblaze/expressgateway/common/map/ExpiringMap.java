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

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.time.Duration;

/**
 * Base Expiring Map Implementation.
 */
public abstract class ExpiringMap<ByteKey> extends Object2IntOpenHashMap<ByteKey> {

    private final Object2LongOpenHashMap<ByteKey> timestampsMap = new Object2LongOpenHashMap<>();
    private final long ttlMillis;

    /**
     * Create a new {@link ExpiringMap} Instance.
     *
     * @param ttlDuration TTL (Time-to-live) Duration of Entries
     */
    public ExpiringMap(Duration ttlDuration) {
        ttlMillis = ttlDuration.toMillis();
    }

    @Override
    public int computeIfAbsent(ByteKey key, Object2IntFunction<? super ByteKey> mappingFunction) {
        final int pos = find(key);
        if (pos >= 0) return value[pos];
        if (!mappingFunction.containsKey(key)) return defRetValue;
        final int newValue = mappingFunction.getInt(key);
        insert(-pos - 1, key, newValue);
        timestampsMap.put(key, System.currentTimeMillis());
        return newValue;
    }

    @Override
    public void clear() {
        timestampsMap.clear();
        super.clear();
    }

    protected boolean isExpired(Object key) {
        return System.currentTimeMillis() - timestampsMap.getLong(key) > ttlMillis;
    }
}
