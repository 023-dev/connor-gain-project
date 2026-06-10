package com.kbt.backend.core.user.presentation.dto;

import jakarta.validation.constraints.Pattern;

public record UserUpdateRequest(
        @Pattern(regexp = NOT_BLANK_IF_PRESENT_PATTERN, message = "닉네임은 공백일 수 없습니다.")
        String nickname,

        @Pattern(regexp = NOT_BLANK_IF_PRESENT_PATTERN, message = "프로필 이미지는 공백일 수 없습니다.")
        String profileImage
) {

    private static final String NOT_BLANK_IF_PRESENT_PATTERN = ".*\\S.*";
}
