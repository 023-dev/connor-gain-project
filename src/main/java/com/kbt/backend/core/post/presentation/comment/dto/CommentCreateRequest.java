package com.kbt.backend.core.post.presentation.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
        @Size(max = CommentValidationConstants.CONTENT_MAX_LENGTH, message = "댓글 내용은 최대 1000자까지 입력 가능합니다.")
        String content
) {
}
