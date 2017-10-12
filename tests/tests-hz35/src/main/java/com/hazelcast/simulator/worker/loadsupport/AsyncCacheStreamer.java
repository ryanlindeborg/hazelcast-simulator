/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.simulator.worker.loadsupport;

import com.hazelcast.cache.ICache;
import com.hazelcast.core.ICompletableFuture;

import java.util.concurrent.Semaphore;

/**
 * Asynchronous implementation of {@link Streamer} for {@link ICache}.
 *
 * @param <K> key type
 * @param <V> value type
 */
final class AsyncCacheStreamer<K, V> extends AbstractAsyncStreamer<K, V> {

    private final ICache<K, V> cache;

    AsyncCacheStreamer(int concurrencyLevel, ICache<K, V> cache) {
        super(concurrencyLevel);
        this.cache = cache;
    }

    AsyncCacheStreamer(int concurrencyLevel, ICache<K, V> cache, Semaphore semaphore) {
        super(concurrencyLevel, semaphore);
        this.cache = cache;
    }

    @Override
    ICompletableFuture storeAsync(K key, V value) {
        return (ICompletableFuture) cache.putAsync(key, value);
    }
}
