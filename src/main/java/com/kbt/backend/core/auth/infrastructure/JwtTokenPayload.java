package com.kbt.backend.core.auth.infrastructure;

import java.time.Instant;

public record JwtTokenPayload(
        String userId,
        Instant expiresAt
) {
}
