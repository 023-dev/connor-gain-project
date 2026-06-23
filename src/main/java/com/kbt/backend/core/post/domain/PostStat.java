package com.kbt.backend.core.post.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "post_stat")
@IdClass(PostId.class)
@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostStat {

    @Id
    @Column(name = "post_id")
    private Long id;

    @Id
    @Column(name = "id2")
    private Long userId;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Column(name = "comment_count", nullable = false)
    private long commentCount;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "post_id", referencedColumnName = "id", insertable = false, updatable = false),
        @JoinColumn(name = "id2", referencedColumnName = "user_id", insertable = false, updatable = false)
    })
    private Post post;

    @Builder
    public PostStat(
            final Long postId,
            final Long id2,
            final long likeCount,
            final long commentCount,
            final long viewCount
    ) {
        this.id = postId;
        this.userId = id2;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
    }

    public void assignId2(final Long id2) {
        this.userId = id2;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}
