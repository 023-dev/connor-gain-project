package com.kbt.backend.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class LocalCacheUtil implements CacheUtil {

    private final Cache<String, CacheEntry<?>> cache;

    @Override
    public <T> Optional<T> get(String key, Class<T> clazz) {
        CacheEntry<?> entry = cache.getIfPresent(key);

        if (entry == null) {
            return Optional.empty();
        }

        if (entry.isExpired()) {
            cache.invalidate(key);
            return Optional.empty();
        }

        try {
            return Optional.of(clazz.cast(entry.value()));
        } catch (ClassCastException e) {
            log.error("[LocalCacheUtil:get]: {} 키에 대한  로컬 캐시 값 가져오기 실패 \n error: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public <T> void set(String key, T value, long ttl) {
        cache.put(key, new CacheEntry<>(value, ttl));
    }
}
