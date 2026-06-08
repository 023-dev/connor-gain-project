package com.kbt.backend.core.user.application.dto;

public record UserSigninResult(
        String userId,
        String nickname,
        String profileImage,
        String accessToken,
        String refreshToken
) {

    public UserSigninResponse toResponse() {
        return new UserSigninResponse(userId, nickname, profileImage, accessToken);
    }
}
