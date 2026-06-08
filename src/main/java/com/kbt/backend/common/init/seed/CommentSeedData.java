package com.kbt.backend.common.init.seed;

import com.kbt.backend.core.post.domain.comment.Comment;

public record CommentSeedData(
        String id,
        String postId,
        String userId,
        String content,
        boolean deleted
) {

    public Comment toComment() {
        return Comment.builder()
                .id(id)
                .postId(postId)
                .userId(userId)
                .content(content)
                .deleted(deleted)
                .build();
    }
}
