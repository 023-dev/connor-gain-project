package com.kbt.backend.core.post.domain;

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
public class Post extends BaseEntity {
    private String id;
    private String userId;
    private String title;
    private String content;
    private String imageUrl;
    private boolean deleted;

    @Builder
    public Post(
            final String id,
            final String userId,
            final String title,
            final String content,
            final String imageUrl,
            final boolean deleted
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.deleted = deleted;
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
        return this.userId.equals(userId);
    }

    public void delete() {
        this.deleted = true;
    }
}
