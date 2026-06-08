package com.kbt.backend.core.post.domain.like;

import com.kbt.backend.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(fluent = true)
public class Like extends BaseEntity {
    private String id;
    private String postId;
    private String userId;
    private boolean deleted;

    @Builder
    public Like(
            final String id,
            final String postId,
            final String userId,
            final boolean deleted
    ) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.deleted = deleted;
    }

    public void activate() {
        this.deleted = false;
    }

    public void delete() {
        this.deleted = true;
    }
}
