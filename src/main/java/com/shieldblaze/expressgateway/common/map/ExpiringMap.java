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

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Base Expiring Map Implementation.
 *
 * @param <K> Key
 * @param <V> Value
 */
public abstract class ExpiringMap<K, V> implements Map<K, V> {

    private final Map<K, V> storageMap;
    private final Map<Object, Long> timestampsMap = new HashMap<>();
    private final long ttlNanos;
    private final boolean autoRenew;
    private final EntryRemovedListener<V> entryRemovedListener;

    /**
     * Create a new {@link ExpiringMap} Instance and use {@link HashMap} as
     * default {@code storageMap} and set {@code autoRenew} to {@code true}.
     *
     * @param ttlDuration TTL (Time-to-live) duration of entries
     */
    public ExpiringMap(Duration ttlDuration) {
        this(new HashMap<>(), ttlDuration, true);
    }

    /**
     * Create a new {@link ExpiringMap} Instance.
     *
     * @param storageMap  {@link Map} Implementation to use for storing entries
     * @param ttlDuration TTL (Time-to-live) Duration of Entries
     * @param autoRenew   Set to {@code true} if entries will be auto-renewed on {@link #get(Object)} call
     *                    else set to {@code false}
     */
    public ExpiringMap(Map<K, V> storageMap, Duration ttlDuration, boolean autoRenew) {
        this(storageMap, ttlDuration, autoRenew, new IgnoreEntryRemovedListener<>());
    }

    /**
     * Create a new {@link ExpiringMap} Instance.
     *
     * @param storageMap           {@link Map} Implementation to use for storing entries
     * @param ttlDuration          TTL (Time-to-live) Duration of Entries
     * @param autoRenew            Set to {@code true} if entries will be auto-renewed on {@link #get(Object)} call
     *                             else set to {@code false}
     * @param entryRemovedListener {@link EntryRemovedListener} Instance
     */
    public ExpiringMap(Map<K, V> storageMap, Duration ttlDuration, boolean autoRenew, EntryRemovedListener<V> entryRemovedListener) {
        this.storageMap = Objects.requireNonNull(storageMap, "StorageMap");
        ttlNanos = ttlDuration.toNanos();
        this.autoRenew = autoRenew;

        if (this.storageMap.size() != 0) {
            throw new IllegalArgumentException("StorageMap Size must be Zero (0).");
        }

        this.entryRemovedListener = Objects.requireNonNull(entryRemovedListener, "EntryRemovedListener");
    }

    @Override
    public int size() {
        return storageMap.size();
    }

    @Override
    public boolean isEmpty() {
        return storageMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return storageMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return storageMap.containsKey(value);
    }

    @Override
    public V get(Object key) {
        V v = storageMap.get(key);
        if (autoRenew) {
            timestampsMap.put(key, System.nanoTime());
        }
        return v;
    }

    @Override
    public V put(K key, V value) {
        timestampsMap.put(key, System.nanoTime());
        return storageMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        timestampsMap.remove(key);
        return storageMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach((BiConsumer<K, V>) this::put);
    }

    @Override
    public void clear() {
        timestampsMap.clear();
        storageMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return storageMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return storageMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return storageMap.entrySet();
    }

    @Override
    public String toString() {
        return storageMap.toString();
    }

    protected EntryRemovedListener<V> entryRemovedListener() {
        return entryRemovedListener;
    }

    protected boolean isExpired(Object key) {
        return System.nanoTime() - timestampsMap.get(key) > ttlNanos;
    }
}
