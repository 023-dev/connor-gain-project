package com.kbt.backend.core.auth.presentation;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public final class RefreshTokenCookie {

    public static final String NAME = "refreshToken";

    private static final Duration MAX_AGE = Duration.ofDays(14);
    private static final String PATH = "/";
    private static final String SAME_SITE = "Lax";

    private RefreshTokenCookie() {
    }

    public static ResponseCookie create(final String refreshToken) {
        return ResponseCookie.from(NAME, refreshToken)
                .httpOnly(true)
                .path(PATH)
                .maxAge(MAX_AGE)
                .sameSite(SAME_SITE)
                .build();
    }

    public static ResponseCookie expire() {
        return ResponseCookie.from(NAME, "")
                .httpOnly(true)
                .path(PATH)
                .maxAge(Duration.ZERO)
                .sameSite(SAME_SITE)
                .build();
    }
}
