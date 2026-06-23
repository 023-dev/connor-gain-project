package com.kbt.backend.core.post.domain.comment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "deleted_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeletedComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "comment_key", columnDefinition = "char(36)", nullable = false)
    private String commentKey;

    @Column(name = "post_key", columnDefinition = "char(36)", nullable = false)
    private String postKey;

    @Column(name = "user_key", columnDefinition = "char(36)", nullable = false)
    private String userKey;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    @Builder
    public DeletedComment(
            final Long commentId,
            final Long postId,
            final Long userId,
            final String commentKey,
            final String postKey,
            final String userKey,
            final String content,
            final LocalDateTime deletedAt
    ) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.commentKey = commentKey;
        this.postKey = postKey;
        this.userKey = userKey;
        this.content = content;
        this.deletedAt = deletedAt != null ? deletedAt : LocalDateTime.now();
    }
}
