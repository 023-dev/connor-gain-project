package com.kbt.backend.core.post.domain;

import java.io.Serializable;
import java.util.Objects;

public class PostId implements Serializable {

    private Long id;
    private Long userId;

    public PostId() {
    }

    public PostId(Long id, Long userId) {
        this.id = id;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostId postId = (PostId) o;
        return Objects.equals(id, postId.id) && Objects.equals(userId, postId.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId);
    }
}
