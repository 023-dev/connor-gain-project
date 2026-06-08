package com.kbt.backend.core.user.application.dto;

public record UserUpdateResponse(
        String userId,
        String nickname,
        String profileImage
) {
}
