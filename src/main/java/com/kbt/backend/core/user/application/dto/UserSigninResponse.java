package com.kbt.backend.core.user.application.dto;

public record UserSigninResponse(
        String userId,
        String nickname,
        String profileImage,
        String accessToken,
        String refreshToken
) {
}
