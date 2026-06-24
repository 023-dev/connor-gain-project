package com.kbt.backend.core.post.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "deleted_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeletedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_key", columnDefinition = "char(36)", nullable = false)
    private String postKey;

    @Column(name = "user_key", columnDefinition = "char(36)", nullable = false)
    private String userKey;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    @Builder
    public DeletedPost(
            final Long postId,
            final Long userId,
            final String postKey,
            final String userKey,
            final String title,
            final String content,
            final String imageUrl,
            final LocalDateTime deletedAt
    ) {
        this.postId = postId;
        this.userId = userId;
        this.postKey = postKey;
        this.userKey = userKey;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.deletedAt = deletedAt != null ? deletedAt : LocalDateTime.now();
    }

    public static DeletedPost from(final Post post) {
        return DeletedPost.builder()
                .postId(post.idLong())
                .userId(post.userIdLong())
                .postKey(post.id())
                .userKey(post.userId())
                .title(post.title())
                .content(post.content())
                .imageUrl(post.imageUrl())
                .build();
    }
}
