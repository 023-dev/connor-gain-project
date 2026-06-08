package com.kbt.backend.core.auth.domain;

import java.time.Instant;

public record RefreshToken(
        String userId,
        String token,
        Instant expiresAt
) {

    public boolean isExpired(final Instant now) {
        return !expiresAt.isAfter(now);
    }
}
