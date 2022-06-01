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

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;

/**
 * {@link SelfExpiringMap} automatically expires entries using a {@link Cleaner}.
 */
public final class SelfExpiringMap extends ExpiringMap<Cache.ByteKey> implements Closeable {

    private final Cleaner cleaner;

    public SelfExpiringMap(Duration duration) {
        super(duration);
        this.cleaner = new DefaultCleaner(this);
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
