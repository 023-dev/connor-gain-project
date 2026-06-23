package com.kbt.backend.core.post.domain.comment;

import com.kbt.backend.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

import static com.kbt.backend.common.utils.Functions.update;

@Entity
@Table(
        name = "comments",
        indexes = {
                @Index(name = "idx_comments_post_id", columnList = "post_id")
        }
)
@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", columnDefinition = "char(36)", nullable = false, unique = true)
    private String key;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "post_key", columnDefinition = "char(36)", nullable = false)
    private String postKey;

    @Column(name = "user_key", columnDefinition = "char(36)", nullable = false)
    private String userKey;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Comment(
            final String id,
            final String postId,
            final String userId,
            final String content,
            final boolean deleted
    ) {
        this.key = id != null ? id : java.util.UUID.randomUUID().toString();
        this.postKey = postId;
        this.userKey = userId;
        this.content = content;
        this.deletedAt = deleted ? LocalDateTime.now() : null;
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

    public Long postIdLong() {
        return this.postId;
    }

    public Long userIdLong() {
        return this.userId;
    }

    public boolean deleted() {
        return this.deletedAt != null;
    }

    public void assignIds(final Long userId, final Long postId) {
        this.userId = userId;
        this.postId = postId;
    }

    public CommentEditor.CommentEditorBuilder toEditor() {
        return CommentEditor.builder()
                .content(content);
    }

    public void edit(final CommentEditor editor) {
        final CommentEditor.CommentEditorBuilder editorBuilder = toEditor();
        update(editorBuilder::content, editor.content());

        final CommentEditor mergedEditor = editorBuilder.build();
        this.content = mergedEditor.content();
    }

    public boolean isWrittenBy(final String userId) {
        return this.userKey.equals(userId);
    }

    public boolean belongsTo(final String postId) {
        return this.postKey.equals(postId);
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
