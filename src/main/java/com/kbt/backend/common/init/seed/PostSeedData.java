package com.kbt.backend.common.init.seed;

import com.kbt.backend.core.post.domain.Post;

public record PostSeedData(
        String id,
        String userId,
        String title,
        String content,
        String imageUrl,
        boolean deleted
) {

    public Post toPost() {
        return Post.builder()
                .id(id)
                .userId(userId)
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .deleted(deleted)
                .build();
    }
}
