package com.kbt.backend.core.post.application.comment.dto;

import com.kbt.backend.core.post.domain.comment.Comment;
import com.kbt.backend.core.user.domain.User;

public record CommentResponse(
        String commentId,
        String postId,
        String userId,
        String nickname,
        String content
) {

    public static CommentResponse from(
            final Comment comment,
            final User user
    ) {
        return new CommentResponse(
                comment.id(),
                comment.postId(),
                comment.userId(),
                user.nickname(),
                comment.content()
        );
    }
}
