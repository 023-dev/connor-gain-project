package com.kbt.backend.core.post.application.like;

import com.kbt.backend.core.post.application.PostQueryService;
import com.kbt.backend.core.post.application.PostCommandService;
import com.kbt.backend.core.post.application.like.dto.LikeResponse;
import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.user.application.UserQueryService;
import com.kbt.backend.core.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeApplicationService {

    private final LikeCommandService likeCommandService;
    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;
    private final UserQueryService userQueryService;

    public LikeResponse like(
            final String userId,
            final String postId
    ) {
        final User user = userQueryService.findActiveUser(userId);
        final Post post = postQueryService.findOne(postId);

        likeCommandService.like(user.id(), post.id());
        final Post updatedPost = postCommandService.increaseLikeCount(post);
        return new LikeResponse(post.id(), updatedPost.likeCount(), true);
    }

    public LikeResponse unlike(
            final String userId,
            final String postId
    ) {
        final User user = userQueryService.findActiveUser(userId);
        final Post post = postQueryService.findOne(postId);

        likeCommandService.unlike(user.id(), post.id());
        final Post updatedPost = postCommandService.decreaseLikeCount(post);
        return new LikeResponse(post.id(), updatedPost.likeCount(), false);
    }
}
