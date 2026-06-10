package com.kbt.backend.core.post.presentation.comment.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
        @Pattern(regexp = CommentValidationConstants.NOT_BLANK_IF_PRESENT_PATTERN, message = "댓글 내용은 공백일 수 없습니다.")
        @Size(max = CommentValidationConstants.CONTENT_MAX_LENGTH, message = "댓글 내용은 최대 1000자까지 입력 가능합니다.")
        String content
) {
}
