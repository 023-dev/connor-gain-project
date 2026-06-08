package com.kbt.backend.core.post.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
        @NotBlank
        @Size(max = PostValidationConstants.TITLE_MAX_LENGTH)
        String title,

        @NotBlank
        @Size(max = PostValidationConstants.CONTENT_MAX_LENGTH)
        String content,

        @Pattern(regexp = PostValidationConstants.NOT_BLANK_IF_PRESENT_PATTERN)
        @Size(max = PostValidationConstants.IMAGE_URL_MAX_LENGTH)
        String imageUrl
) {
}
