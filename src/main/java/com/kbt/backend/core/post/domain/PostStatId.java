package com.kbt.backend.core.post.domain;

import java.io.Serializable;
import java.util.Objects;

public class PostStatId implements Serializable {

    private Long postId;
    private Long id2;

    public PostStatId() {
    }

    public PostStatId(Long postId, Long id2) {
        this.postId = postId;
        this.id2 = id2;
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
        PostStatId that = (PostStatId) o;
        return Objects.equals(postId, that.postId) && Objects.equals(id2, that.id2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, id2);
    }
}
