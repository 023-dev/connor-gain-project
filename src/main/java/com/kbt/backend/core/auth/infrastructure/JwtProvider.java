package com.kbt.backend.core.auth.infrastructure;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private static final String TOKEN_TYPE = "token_type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final SecretKey secretKey;
    private final Duration accessTokenValidity;
    private final Duration refreshTokenValidity;

    public JwtProvider(
            @Value("${jwt.secret-key}") final String secretKeyString,
            @Value("${jwt.access-token-validity}") final Duration accessTokenValidity,
            @Value("${jwt.refresh-token-validity}") final Duration refreshTokenValidity
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    public String createAccessToken(final String userId) {
        return createToken(userId, ACCESS, accessTokenValidity);
    }

    public String createRefreshToken(final String userId) {
        return createToken(userId, REFRESH, refreshTokenValidity);
    }

    public JwtTokenPayload parseAccessTokenPayload(final String token) {
        final Claims claims = parseClaims(token, ACCESS, ErrorType.UNAUTHORIZED);
        return new JwtTokenPayload(claims.getSubject(), claims.getExpiration().toInstant());
    }

    public JwtTokenPayload parseRefreshTokenPayload(final String token) {
        final Claims claims = parseClaims(token, REFRESH, ErrorType.INVALID_TOKEN);
        return new JwtTokenPayload(claims.getSubject(), claims.getExpiration().toInstant());
    }

    private String createToken(final String userId, final String type, final Duration validity) {
        final Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .claim(TOKEN_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(validity)))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(
            final String token,
            final String expectedType,
            final ErrorType errorCode
    ) {
        try {
            final Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!expectedType.equals(claims.get(TOKEN_TYPE, String.class))) {
                throw new ApiException(errorCode);
            }

            return claims;
        } catch (final JwtException exception) {
            throw new ApiException(errorCode);
        }
    }
}
