package com.kbt.backend.core.user.presentation.dto;

import jakarta.validation.constraints.Pattern;

public record UserUpdateRequest(
        @Pattern(regexp = NOT_BLANK_IF_PRESENT_PATTERN)
        String nickname,

        @Pattern(regexp = NOT_BLANK_IF_PRESENT_PATTERN)
        String profileImage
) {

    private static final String NOT_BLANK_IF_PRESENT_PATTERN = ".*\\S.*";
}
