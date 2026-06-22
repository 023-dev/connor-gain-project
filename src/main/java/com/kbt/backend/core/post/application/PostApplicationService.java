package com.kbt.backend.core.post.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.post.application.dto.PostCreateResponse;
import com.kbt.backend.core.post.application.dto.PostResponse;
import com.kbt.backend.core.post.application.dto.PostsResponse;
import com.kbt.backend.core.post.application.dto.PostUpdateResponse;
import com.kbt.backend.core.post.application.like.LikeQueryService;
import com.kbt.backend.common.domain.CursorPage;
import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.user.application.UserQueryService;
import com.kbt.backend.core.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostApplicationService {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;
    private final LikeQueryService likeQueryService;
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

    public PostResponse findOne(
            final Optional<String> userId,
            final String postId
    ) {
        final Post post = postQueryService.findOne(postId);
        final User user = userQueryService.findUser(post.userId());
        final Post viewedPost = postCommandService.increaseViewCount(post);
        return PostResponse.from(viewedPost, user, likeQueryService.isLikedByUser(post.id(), userId));
    }

    public PostsResponse findAll(
            final Optional<String> userId,
            final String cursor,
            final int size
    ) {
        final CursorPage<Post> page = postQueryService.findAll(cursor, size);

        final List<PostResponse> posts = page.items().stream()
                .map(post -> PostResponse.from(
                        post,
                        userQueryService.findUser(post.userId()),
                        likeQueryService.isLikedByUser(post.id(), userId)
                ))
                .toList();

        return new PostsResponse(posts, page.nextCursor(), page.hasNext());
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
}
