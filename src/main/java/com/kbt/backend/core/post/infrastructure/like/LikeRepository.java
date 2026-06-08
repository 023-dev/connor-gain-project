package com.kbt.backend.core.post.infrastructure.like;

import com.kbt.backend.core.post.domain.like.Like;

import java.util.Optional;

public interface LikeRepository {
    Like save(final Like like);

    Optional<Like> findByPostIdAndUserId(
            final String postId,
            final String userId
    );

    Optional<Like> findActiveByPostIdAndUserId(
            final String postId,
            final String userId
    );

    long countActiveByPostId(final String postId);
}
