package com.kbt.backend.core.auth.infrastructure;

import com.kbt.backend.core.auth.domain.RefreshToken;

import java.time.Instant;
import java.util.Optional;

public interface TokenRepository {

    void saveRefreshToken(final RefreshToken refreshToken);

    Optional<RefreshToken> consumeRefreshToken(final String token);

    void deleteRefreshTokensByUserId(final String userId);

    void saveRevokedAccessToken(
            final String token,
            final Instant expiresAt
    );

    boolean existsRevokedAccessToken(final String token);

    void deleteExpiredRevokedAccessTokens(final Instant now);
}
