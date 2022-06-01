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

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Base Expiring Map Implementation.
 */
public abstract class ExpiringMap<ByteKey> extends Object2IntOpenHashMap<ByteKey> {

    private final Map<Object, Long> timestampsMap = new HashMap<>();
    private final long ttlMillis;
    private final EntryRemovedListener<ByteKey> entryRemovedListener;

    /**
     * Create a new {@link ExpiringMap} Instance.
     *
     * @param ttlDuration TTL (Time-to-live) Duration of Entries
     */
    public ExpiringMap(Duration ttlDuration) {
        ttlMillis = ttlDuration.toMillis();
        this.entryRemovedListener = new IgnoreEntryRemovedListener<>();
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return super.containsKey(value);
    }

    @Override
    public Integer get(Object key) {
        return super.get(key);
    }

    @Override
    public Integer put(ByteKey key, Integer value) {
        timestampsMap.put(key, System.currentTimeMillis());
        return super.put(key, value);
    }

    @Override
    public Integer remove(Object key) {
        timestampsMap.remove(key);
        return super.remove(key);
    }

    @Override
    public void clear() {
        timestampsMap.clear();
        super.clear();
    }

    protected EntryRemovedListener<ByteKey> entryRemovedListener() {
        return entryRemovedListener;
    }

    protected boolean isExpired(Object key) {
        return System.currentTimeMillis() - timestampsMap.get(key) > ttlMillis;
    }
}
