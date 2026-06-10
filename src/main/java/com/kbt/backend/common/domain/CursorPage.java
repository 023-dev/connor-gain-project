package com.kbt.backend.common.domain;

import java.util.List;

public record CursorPage<T>(
        List<T> items,
        String nextCursor,
        boolean hasNext
) {
}
