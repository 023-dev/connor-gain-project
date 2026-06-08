package com.kbt.backend.core.post.application;

import com.kbt.backend.core.post.application.dto.PostCreateResponse;
import com.kbt.backend.core.post.application.dto.PostResponse;
import com.kbt.backend.core.post.application.dto.PostsResponse;
import com.kbt.backend.core.post.application.dto.PostUpdateResponse;
import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.user.application.UserQueryService;
import com.kbt.backend.core.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostApplicationService {

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
        return PostResponse.from(post, user);
    }

    public PostsResponse findAll() {
        final List<PostResponse> posts = postQueryService.findAll().stream()
                .map(post -> PostResponse.from(post, userQueryService.findActiveUser(post.userId())))
                .toList();

        return new PostsResponse(posts);
    }

    public PostUpdateResponse edit(
            final String userId,
            final String postId,
            final String title,
            final String content,
            final String imageUrl
    ) {
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
}
