package com.kbt.backend.core.post.application.comment.dto;

import java.util.List;

public record CommentsResponse(
        List<CommentResponse> comments
) {
}
