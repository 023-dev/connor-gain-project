package com.kbt.backend.core.post.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.common.domain.CursorPage;
import com.kbt.backend.common.utils.CursorUtils;
import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.post.infrastructure.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;

    public Post findOne(final String postId) {
        return postRepository.findActiveById(postId)
                .orElseThrow(() -> new ApiException(ErrorType.POST_NOT_FOUND));
    }

    public List<Post> findAll() {
        return postRepository.findAllActive();
    }

    public CursorPage<Post> findAll(final String cursor, final int size) {
        final List<String> cursorValues = CursorUtils.decode(cursor);
        validateCursor(cursorValues);

        final List<Post> pagePosts = postRepository.findAllActiveByCursor(cursorValues, size + 1);

        final boolean hasNext = pagePosts.size() > size;
        final List<Post> visiblePosts = hasNext ? pagePosts.subList(0, size) : pagePosts;
        final String nextCursor = hasNext
                ? CursorUtils.encode(visiblePosts.getLast().getCreatedAt().toString(), visiblePosts.getLast().id())
                : null;

        return new CursorPage<>(visiblePosts, nextCursor, hasNext);
    }

    private void validateCursor(final List<String> cursorValues) {
        if (cursorValues == null) {
            return;
        }

        try {
            if (cursorValues.size() != 2) {
                throw new IllegalArgumentException();
            }
            LocalDateTime.parse(cursorValues.get(0));
            if (!StringUtils.hasText(cursorValues.get(1))) {
                throw new IllegalArgumentException();
            }
        } catch (Exception exception) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }
}
