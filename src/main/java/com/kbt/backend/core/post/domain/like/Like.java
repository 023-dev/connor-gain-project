package com.kbt.backend.core.post.domain.like;

import com.kbt.backend.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(
        name = "post_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"})
)
@IdClass(LikeId.class)
@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "like_seq")
    @SequenceGenerator(name = "like_seq", sequenceName = "like_seq", allocationSize = 1)
    private Long id;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "post_id")
    private Long postId;

    @Id
    @Column(name = "id2")
    private Long id2;

    @Column(name = "external_id", columnDefinition = "char(36)", nullable = false, unique = true)
    private String key;

    @Column(name = "user_key", columnDefinition = "char(36)", nullable = false)
    private String userKey;

    @Column(name = "post_key", columnDefinition = "char(36)", nullable = false)
    private String postKey;

    @Column(nullable = false)
    private boolean deleted;

    @Builder
    public Like(
            final String id,
            final String postId,
            final String userId,
            final boolean deleted
    ) {
        this.key = id != null ? id : java.util.UUID.randomUUID().toString();
        this.postKey = postId;
        this.userKey = userId;
        this.deleted = deleted;
    }

    public String id() {
        return this.key;
    }

    public String postId() {
        return this.postKey;
    }

    public String userId() {
        return this.userKey;
    }

    public Long idLong() {
        return this.id;
    }

    public Long userIdLong() {
        return this.userId;
    }

    public Long postIdLong() {
        return this.postId;
    }

    public Long id2Long() {
        return this.id2;
    }

    public void assignIds(final Long userId, final Long postId, final Long id2) {
        this.userId = userId;
        this.postId = postId;
        this.id2 = id2;
    }

    public void activate() {
        this.deleted = false;
    }

    public void delete() {
        this.deleted = true;
    }
}
