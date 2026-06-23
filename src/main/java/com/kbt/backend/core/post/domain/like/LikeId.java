package com.kbt.backend.core.post.domain.like;

import java.io.Serializable;
import java.util.Objects;

public class LikeId implements Serializable {

    private Long id;
    private Long userId;
    private Long postId;
    private Long id2;

    public LikeId() {
    }

    public LikeId(Long id, Long userId, Long postId, Long id2) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
        this.id2 = id2;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getId2() {
        return id2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LikeId likeId = (LikeId) o;
        return Objects.equals(id, likeId.id) &&
                Objects.equals(userId, likeId.userId) &&
                Objects.equals(postId, likeId.postId) &&
                Objects.equals(id2, likeId.id2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, postId, id2);
    }
}
