package com.kbt.backend.common.cache;

public class CacheEntry<T> {
    private final T value;
    private final long expiredAt;

    public CacheEntry(T value, long ttl) {
        this.value = value;
        this.expiredAt = System.currentTimeMillis() + ttl;
    }

    public T value() {
        return value;
    }

    public boolean isExpired() {
        return expiredAt < System.currentTimeMillis();
    }
}
