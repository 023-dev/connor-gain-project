package com.kbt.backend.core.post.presentation.comment.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
        @Pattern(regexp = CommentValidationConstants.NOT_BLANK_IF_PRESENT_PATTERN)
        @Size(max = CommentValidationConstants.CONTENT_MAX_LENGTH)
        String content
) {
}
