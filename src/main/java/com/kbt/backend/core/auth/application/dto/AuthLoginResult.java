package com.kbt.backend.core.auth.application.dto;

public record AuthLoginResult(
        String userId,
        String nickname,
        String profileImage,
        String accessToken,
        String refreshToken
) {

    public AuthLoginResponse toResponse() {
        return new AuthLoginResponse(userId, nickname, profileImage, accessToken);
    }
}
