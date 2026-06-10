package com.kbt.backend.core.post.application.dto;

import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.user.domain.User;
import java.time.LocalDateTime;

public record PostResponse(
        String postId,
        String userId,
        String nickname,
        String title,
        String content,
        String imageUrl,
        long likeCount,
        long commentCount,
        long viewCount,
        boolean isLiked,
        LocalDateTime createdAt
) {

    public static PostResponse from(
            final Post post,
            final User user,
            final boolean isLiked
    ) {
        return new PostResponse(
                post.id(),
                post.userId(),
                user.nickname(),
                post.title(),
                post.content(),
                post.imageUrl(),
                post.likeCount(),
                post.commentCount(),
                post.viewCount(),
                isLiked,
                post.getCreatedAt()
        );
    }
}
