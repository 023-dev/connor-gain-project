package com.kbt.backend.core.auth.infrastructure;

import com.kbt.backend.core.auth.domain.RefreshToken;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryTokenRepository implements TokenRepository {

    private final ConcurrentMap<String, RefreshToken> refreshTokens = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Instant> revokedAccessTokens = new ConcurrentHashMap<>();

    @Override
    public void saveRefreshToken(final RefreshToken refreshToken) {
        refreshTokens.put(refreshToken.token(), refreshToken);
    }

    @Override
    public Optional<RefreshToken> consumeRefreshToken(final String token) {
        return Optional.ofNullable(refreshTokens.remove(token));
    }

    @Override
    public void deleteRefreshTokensByUserId(final String userId) {
        refreshTokens.entrySet()
                .removeIf(entry -> entry.getValue().userId().equals(userId));
    }

    @Override
    public void saveRevokedAccessToken(
            final String token,
            final Instant expiresAt
    ) {
        revokedAccessTokens.put(token, expiresAt);
    }

    @Override
    public boolean existsRevokedAccessToken(final String token) {
        return revokedAccessTokens.containsKey(token);
    }

    @Override
    public void deleteExpiredRevokedAccessTokens(final Instant now) {
        revokedAccessTokens.entrySet()
                .removeIf(entry -> !entry.getValue().isAfter(now));
    }
}
