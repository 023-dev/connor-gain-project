package com.kbt.backend.core.post.presentation.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank
        @Size(max = CommentValidationConstants.CONTENT_MAX_LENGTH)
        String content
) {
}
