package com.kbt.backend.core.auth.application.dto;

public record AuthTokenResult(
        String accessToken,
        String refreshToken
) {
}
