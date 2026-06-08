package com.kbt.backend.core.user.application.dto;

public record UserMeResponse(
        String userId,
        String email,
        String nickname,
        String profileImage
) {
}
