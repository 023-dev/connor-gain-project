package com.kbt.backend.core.post.application.dto;

import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.user.domain.User;

public record PostResponse(
        String postId,
        String userId,
        String nickname,
        String title,
        String content,
        String imageUrl
) {

    public static PostResponse from(
            final Post post,
            final User user
    ) {
        return new PostResponse(
                post.id(),
                post.userId(),
                user.nickname(),
                post.title(),
                post.content(),
                post.imageUrl()
        );
    }
}
