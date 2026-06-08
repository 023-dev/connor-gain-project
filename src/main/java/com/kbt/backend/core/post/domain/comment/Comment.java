package com.kbt.backend.core.post.domain.comment;

import com.kbt.backend.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.kbt.backend.common.utils.Functions.update;

@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(fluent = true)
public class Comment extends BaseEntity {
    private String id;
    private String postId;
    private String userId;
    private String content;
    private boolean deleted;

    @Builder
    public Comment(
            final String id,
            final String postId,
            final String userId,
            final String content,
            final boolean deleted
    ) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.deleted = deleted;
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
        return this.userId.equals(userId);
    }

    public boolean belongsTo(final String postId) {
        return this.postId.equals(postId);
    }

    public void delete() {
        this.deleted = true;
    }
}
