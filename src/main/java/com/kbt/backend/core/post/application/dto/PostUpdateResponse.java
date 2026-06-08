package com.kbt.backend.core.post.application.dto;

import com.kbt.backend.core.post.domain.Post;

public record PostUpdateResponse(
        String postId,
        String title,
        String content,
        String imageUrl
) {

    public static PostUpdateResponse from(final Post post) {
        return new PostUpdateResponse(
                post.id(),
                post.title(),
                post.content(),
                post.imageUrl()
        );
    }
}
