package com.kbt.backend.common.cache;

import java.util.Optional;

public interface CacheUtil {
    public <T> Optional<T> get(String key, Class<T> clazz);

    public <T> void set(String key, T value, long ttl);
}
