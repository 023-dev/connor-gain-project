package com.kbt.backend.core.auth.application.dto;

public record AuthLoginResponse(
        String userId,
        String nickname,
        String profileImage,
        String accessToken
) {
}
