package com.kbt.backend.core.post.application;

public interface PostStatBuffer {
    void incrementLikeCount(final Long postId);
    void decrementLikeCount(final Long postId);
    void incrementCommentCount(final Long postId);
    void decrementCommentCount(final Long postId);
    void incrementViewCount(final Long postId);
    long pendingLikeDelta(final Long postId);
    long pendingCommentDelta(final Long postId);
    long pendingViewDelta(final Long postId);
    void flush();
}
