package com.kbt.backend.core.post.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = PostValidationConstants.TITLE_MAX_LENGTH, message = "제목은 최대 100자까지 입력 가능합니다.")
        String title,

        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @Size(max = PostValidationConstants.CONTENT_MAX_LENGTH, message = "내용은 최대 5000자까지 입력 가능합니다.")
        String content,

        @Pattern(regexp = PostValidationConstants.NOT_BLANK_IF_PRESENT_PATTERN, message = "이미지 URL은 공백일 수 없습니다.")
        @Size(max = PostValidationConstants.IMAGE_URL_MAX_LENGTH, message = "이미지 URL은 최대 2048자까지 입력 가능합니다.")
        String imageUrl
) {
}
