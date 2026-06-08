package com.kbt.backend.core.post.application.comment.dto;

import com.kbt.backend.core.post.domain.comment.Comment;

public record CommentUpdateResponse(
        String commentId,
        String content
) {

    public static CommentUpdateResponse from(final Comment comment) {
        return new CommentUpdateResponse(
                comment.id(),
                comment.content()
        );
    }
}
