package com.kbt.backend.core.post.domain;

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

import static com.kbt.backend.common.utils.Functions.update;

@Entity
@Table(name = "posts")
@IdClass(PostId.class)
@SQLDelete(sql = "UPDATE posts SET deleted_at = CURRENT_TIMESTAMP(6), title = '삭제된 게시글입니다.', content = '삭제된 게시글입니다.', image_url = null WHERE id = ? AND user_id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_seq")
    @SequenceGenerator(name = "post_seq", sequenceName = "post_seq", allocationSize = 1)
    private Long id;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "external_id", columnDefinition = "char(36)", nullable = false, unique = true)
    private String key;

    @Column(name = "user_key", columnDefinition = "char(36)", nullable = false)
    private String userKey;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PostStat stat;

    @Builder
    public Post(
            final String id,
            final String userId,
            final String title,
            final String content,
            final String imageUrl,
            final long likeCount,
            final long commentCount,
            final long viewCount,
            final LocalDateTime deletedAt
    ) {
        this.key = id != null ? id : java.util.UUID.randomUUID().toString();
        this.userKey = userId;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.deletedAt = deletedAt;
        this.stat = null;
    }

    public String id() {
        return this.key;
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

    public void assignUserId(final Long userId) {
        this.userId = userId;
    }

    public void assignId(final Long id) {
        this.id = id;
    }

    public void assignStat(final PostStat stat) {
        this.stat = stat;
    }

    public long likeCount() {
        return this.stat != null ? this.stat.likeCount() : 0L;
    }

    public long commentCount() {
        return this.stat != null ? this.stat.commentCount() : 0L;
    }

    public long viewCount() {
        return this.stat != null ? this.stat.viewCount() : 0L;
    }

    public PostEditor.PostEditorBuilder toEditor() {
        return PostEditor.builder()
                .title(title)
                .content(content)
                .imageUrl(imageUrl);
    }

    public void edit(final PostEditor editor) {
        final PostEditor.PostEditorBuilder editorBuilder = toEditor();
        update(editorBuilder::title, editor.title());
        update(editorBuilder::content, editor.content());
        update(editorBuilder::imageUrl, editor.imageUrl());

        final PostEditor mergedEditor = editorBuilder.build();
        this.title = mergedEditor.title();
        this.content = mergedEditor.content();
        this.imageUrl = mergedEditor.imageUrl();
    }

    public boolean isWrittenBy(final String userId) {
        return this.userKey.equals(userId);
    }

    public void incrementLikeCount() {
        ensureStatInitialized();
        this.stat.incrementLikeCount();
    }

    public void decrementLikeCount() {
        ensureStatInitialized();
        this.stat.decrementLikeCount();
    }

    public void incrementCommentCount() {
        ensureStatInitialized();
        this.stat.incrementCommentCount();
    }

    public void decrementCommentCount() {
        ensureStatInitialized();
        this.stat.decrementCommentCount();
    }

    public void incrementViewCount() {
        ensureStatInitialized();
        this.stat.incrementViewCount();
    }

    private void ensureStatInitialized() {
        if (this.stat == null) {
            this.stat = PostStat.builder()
                    .postId(this.id)
                    .id2(this.userId)
                    .likeCount(0)
                    .commentCount(0)
                    .viewCount(0)
                    .build();
        }
    }

    public boolean deleted() {
        return this.deletedAt != null;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
