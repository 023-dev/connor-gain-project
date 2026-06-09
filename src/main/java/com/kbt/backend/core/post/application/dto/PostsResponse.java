package com.kbt.backend.core.post.application.dto;

import java.util.List;

public record PostsResponse(
        List<PostResponse> posts,
        String nextCursor,
        boolean hasNext
) {
}
