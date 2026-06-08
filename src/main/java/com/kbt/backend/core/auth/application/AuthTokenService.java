package com.kbt.backend.core.auth.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.auth.application.dto.AuthTokenResult;
import com.kbt.backend.core.auth.domain.RefreshToken;
import com.kbt.backend.core.auth.infrastructure.JwtProvider;
import com.kbt.backend.core.auth.infrastructure.JwtTokenPayload;
import com.kbt.backend.core.auth.infrastructure.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;

    public AuthTokenResult issue(final String userId) {
        final String accessToken = jwtProvider.createAccessToken(userId);
        final String refreshToken = jwtProvider.createRefreshToken(userId);
        final JwtTokenPayload refreshPayload = jwtProvider.parseRefreshTokenPayload(refreshToken);

        tokenRepository.saveRefreshToken(new RefreshToken(userId, refreshToken, refreshPayload.expiresAt()));
        return new AuthTokenResult(accessToken, refreshToken);
    }

    public String parseAccessToken(final String token) {
        final Instant now = Instant.now();
        tokenRepository.deleteExpiredRevokedAccessTokens(now);

        if (tokenRepository.existsRevokedAccessToken(token)) {
            throw new ApiException(ErrorType.UNAUTHORIZED);
        }

        return jwtProvider.parseAccessTokenPayload(token).userId();
    }

    public String parseRefreshToken(final String token) {
        return jwtProvider.parseRefreshTokenPayload(token).userId();
    }

    public AuthTokenResult reissueRefreshToken(
            final String refreshToken,
            final String userId
    ) {
        final RefreshToken savedRefreshToken = tokenRepository.consumeRefreshToken(refreshToken)
                .orElseThrow(() -> new ApiException(ErrorType.INVALID_TOKEN));

        if (!savedRefreshToken.userId().equals(userId) || savedRefreshToken.isExpired(Instant.now())) {
            throw new ApiException(ErrorType.INVALID_TOKEN);
        }

        return issue(userId);
    }

    public void revokeAccessToken(final String token) {
        final JwtTokenPayload payload = jwtProvider.parseAccessTokenPayload(token);
        tokenRepository.saveRevokedAccessToken(token, payload.expiresAt());
    }

    public void revokeRefreshTokens(final String userId) {
        tokenRepository.deleteRefreshTokensByUserId(userId);
    }
}
