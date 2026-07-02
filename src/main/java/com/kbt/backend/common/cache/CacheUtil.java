package com.kbt.backend.common.cache;

import java.util.Optional;

public interface CacheUtil {
    <T> Optional<T> get(String key, Class<T> clazz);

    <T> void set(String key, T value, long ttlMillis);
}
