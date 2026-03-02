/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rlink.project.common.biz.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.rlink.project.config.GotoLocalCacheConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 短链接跳转本地缓存，基于 Caffeine 的 Window TinyLFU 策略。
 */
@Component
public class ShortLinkGotoLocalCache {

    private final GotoLocalCacheConfiguration gotoLocalCacheConfiguration;

    private Cache<String, CacheValue> cache;

    public ShortLinkGotoLocalCache(GotoLocalCacheConfiguration gotoLocalCacheConfiguration) {
        this.gotoLocalCacheConfiguration = gotoLocalCacheConfiguration;
    }

    @PostConstruct
    public void init() {
        cache = Caffeine.newBuilder()
                .maximumSize(Optional.ofNullable(gotoLocalCacheConfiguration.getMaximumSize()).orElse(10000L))
                .expireAfter(new Expiry<String, CacheValue>() {
                    @Override
                    public long expireAfterCreate(String key, CacheValue value, long currentTime) {
                        return value.remainingNanos();
                    }

                    @Override
                    public long expireAfterUpdate(String key, CacheValue value, long currentTime, long currentDuration) {
                        return value.remainingNanos();
                    }

                    @Override
                    public long expireAfterRead(String key, CacheValue value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();
    }

    public CacheValue get(String fullShortUrl) {
        if (!isEnabled()) {
            return null;
        }
        return cache.getIfPresent(fullShortUrl);
    }

    public void putOrigin(String fullShortUrl, String originUrl, long duration, TimeUnit timeUnit) {
        if (!isEnabled()) {
            return;
        }
        cache.put(fullShortUrl, CacheValue.origin(originUrl, duration, timeUnit));
    }

    public void putNullMarker(String fullShortUrl, long duration, TimeUnit timeUnit) {
        if (!isEnabled()) {
            return;
        }
        cache.put(fullShortUrl, CacheValue.nullMarker(duration, timeUnit));
    }

    public void invalidate(String fullShortUrl) {
        if (!isEnabled()) {
            return;
        }
        cache.invalidate(fullShortUrl);
    }

    private boolean isEnabled() {
        return Boolean.TRUE.equals(gotoLocalCacheConfiguration.getEnabled());
    }

    @Getter
    public static final class CacheValue {

        private final String originUrl;

        private final boolean nullMarker;

        private final long expireAt;

        private CacheValue(String originUrl, boolean nullMarker, long expireAt) {
            this.originUrl = originUrl;
            this.nullMarker = nullMarker;
            this.expireAt = expireAt;
        }

        public static CacheValue origin(String originUrl, long duration, TimeUnit timeUnit) {
            return new CacheValue(originUrl, false, System.currentTimeMillis() + timeUnit.toMillis(duration));
        }

        public static CacheValue nullMarker(long duration, TimeUnit timeUnit) {
            return new CacheValue(null, true, System.currentTimeMillis() + timeUnit.toMillis(duration));
        }

        public long remainingNanos() {
            long remainingMillis = expireAt - System.currentTimeMillis();
            if (remainingMillis <= 0) {
                return 0L;
            }
            return TimeUnit.MILLISECONDS.toNanos(remainingMillis);
        }
    }
}