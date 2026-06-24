package com.kbt.backend.core.post.domain.like;

import com.kbt.backend.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

import com.kbt.backend.common.utils.UuidGenerator;

@Entity
@Table(
        name = "post_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"})
)
@SQLDelete(sql = "UPDATE post_likes SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "external_id", columnDefinition = "char(36)", nullable = false, unique = true)
    private String key;

    @Column(name = "user_key", columnDefinition = "char(36)", nullable = false)
    private String userKey;

    @Column(name = "post_key", columnDefinition = "char(36)", nullable = false)
    private String postKey;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Like(
            final String id,
            final String postId,
            final String userId,
            final LocalDateTime deletedAt
    ) {
        this.key = id != null ? id : UuidGenerator.generate();
        this.postKey = postId;
        this.userKey = userId;
        this.deletedAt = deletedAt;
    }

    public static Like create(
            final String userKey,
            final Long userId,
            final String postKey,
            final Long postId
    ) {
        final Like like = Like.builder()
                .postId(postKey)
                .userId(userKey)
                .build();
        like.assignIds(userId, postId);
        return like;
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

    public void assignIds(final Long userId, final Long postId) {
        this.userId = userId;
        this.postId = postId;
    }

    public boolean deleted() {
        return this.deletedAt != null;
    }

    public void activate() {
        this.deletedAt = null;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
