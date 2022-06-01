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

import com.aayushatharva.seqc.utils.Cache;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.io.Closeable;
import java.io.IOException;

/**
 * Base implementation of Cleaner for auto-removing expired entries.
 */
public abstract class Cleaner implements Runnable, Closeable {

    private final SelfExpiringMap selfExpiringMap;

    public Cleaner(SelfExpiringMap selfExpiringMap) {
        this.selfExpiringMap = selfExpiringMap;
    }

    @Override
    public void run() {
        for (Object2IntMap.Entry<Cache.ByteKey> entry : selfExpiringMap.object2IntEntrySet()) {
            if (selfExpiringMap.isExpired(entry.getKey())) {
                selfExpiringMap.removeInt(entry.getKey());
            }
        }
    }

    @Override
    public void close() throws IOException {
        selfExpiringMap.clear();
    }
}
