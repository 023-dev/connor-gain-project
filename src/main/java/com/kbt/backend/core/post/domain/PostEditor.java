package com.kbt.backend.core.post.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class PostEditor {

    private final String title;
    private final String content;
    private final String imageUrl;

    @Builder
    public PostEditor(
            final String title,
            final String content,
            final String imageUrl
    ) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }
}
