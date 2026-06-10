package com.kbt.backend.core.auth.application.dto;

public record AuthToken(
        String accessToken,
        String refreshToken
) {
}
