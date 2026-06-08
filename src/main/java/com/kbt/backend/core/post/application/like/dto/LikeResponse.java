package com.kbt.backend.core.post.application.like.dto;

public record LikeResponse(
        String postId,
        long likeCount,
        boolean isLiked
) {
}
