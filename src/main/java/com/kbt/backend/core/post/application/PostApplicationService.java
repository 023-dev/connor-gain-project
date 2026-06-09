package com.kbt.backend.core.post.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.post.application.dto.PostCreateResponse;
import com.kbt.backend.core.post.application.dto.PostResponse;
import com.kbt.backend.core.post.application.dto.PostsResponse;
import com.kbt.backend.core.post.application.dto.PostUpdateResponse;
import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.user.application.UserQueryService;
import com.kbt.backend.core.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostApplicationService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 20;

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;
    private final UserQueryService userQueryService;

    public PostCreateResponse create(
            final String userId,
            final String title,
            final String content,
            final String imageUrl
    ) {
        userQueryService.findActiveUser(userId);

        final Post post = postCommandService.create(userId, title, content, imageUrl);
        return new PostCreateResponse(post.id());
    }

    public PostResponse findOne(final String postId) {
        final Post post = postQueryService.findOne(postId);
        final User user = userQueryService.findActiveUser(post.userId());
        final Post viewedPost = postCommandService.increaseViewCount(post);
        return PostResponse.from(viewedPost, user);
    }

    public PostsResponse findAll(
            final String cursor,
            final Integer size
    ) {
        final int pageSize = resolvePageSize(size);
        final PostCursor pageCursor = parseCursor(cursor);

        final List<Post> pagePosts = postQueryService.findAll().stream()
                .filter(post -> isAfterCursor(post, pageCursor))
                .limit(pageSize + 1L)
                .toList();

        final boolean hasNext = pagePosts.size() > pageSize;
        final List<Post> visiblePosts = hasNext ? pagePosts.subList(0, pageSize) : pagePosts;
        final String nextCursor = hasNext ? encodeCursor(visiblePosts.getLast()) : null;

        final List<PostResponse> posts = visiblePosts.stream()
                .map(post -> PostResponse.from(post, userQueryService.findActiveUser(post.userId())))
                .toList();

        return new PostsResponse(posts, nextCursor, hasNext);
    }

    public PostUpdateResponse edit(
            final String userId,
            final String postId,
            final String title,
            final String content,
            final String imageUrl
    ) {
        validateHasEditValue(title, content, imageUrl);

        final User user = userQueryService.findActiveUser(userId);
        final Post post = postQueryService.findOne(postId);

        final Post editedPost = postCommandService.edit(user.id(), post, title, content, imageUrl);
        return PostUpdateResponse.from(editedPost);
    }

    public void delete(
            final String userId,
            final String postId
    ) {
        final User user = userQueryService.findActiveUser(userId);
        final Post post = postQueryService.findOne(postId);

        postCommandService.delete(user.id(), post);
    }

    private void validateHasEditValue(
            final String title,
            final String content,
            final String imageUrl
    ) {
        if (!StringUtils.hasText(title) && !StringUtils.hasText(content) && !StringUtils.hasText(imageUrl)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }

    private int resolvePageSize(final Integer size) {
        final int resolvedSize = size == null ? DEFAULT_PAGE_SIZE : size;
        if (resolvedSize < 1 || resolvedSize > MAX_PAGE_SIZE) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
        return resolvedSize;
    }

    private PostCursor parseCursor(final String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        try {
            final String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            final String[] parts = decoded.split("\\|", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException();
            }

            return new PostCursor(LocalDateTime.parse(parts[0]), parts[1]);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }

    private boolean isAfterCursor(
            final Post post,
            final PostCursor cursor
    ) {
        if (cursor == null) {
            return true;
        }

        if (post.getCreatedAt().isBefore(cursor.createdAt())) {
            return true;
        }

        if (post.getCreatedAt().isEqual(cursor.createdAt())) {
            return post.id().compareTo(cursor.postId()) < 0;
        }

        return false;
    }

    private String encodeCursor(final Post post) {
        final String value = post.getCreatedAt() + "|" + post.id();
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private record PostCursor(
            LocalDateTime createdAt,
            String postId
    ) {
    }
}
