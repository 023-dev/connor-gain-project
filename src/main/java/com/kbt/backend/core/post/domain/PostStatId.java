package com.kbt.backend.core.post.domain;

import java.io.Serializable;
import java.util.Objects;

public class PostStatId implements Serializable {

    private Long id;
    private Long id2;

    public PostStatId() {
    }

    public PostStatId(Long id, Long id2) {
        this.id = id;
        this.id2 = id2;
    }

    public Long getId() {
        return id;
    }

    public Long getId2() {
        return id2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostStatId that = (PostStatId) o;
        return Objects.equals(id, that.id) && Objects.equals(id2, that.id2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, id2);
    }
}
