package com.kbt.backend.core.post.domain.comment;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class CommentEditor {

    private final String content;

    @Builder
    public CommentEditor(final String content) {
        this.content = content;
    }
}
