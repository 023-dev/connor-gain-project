package com.kbt.backend.core.post.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.common.utils.UuidGenerator;
import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.post.domain.PostEditor;
import com.kbt.backend.core.post.domain.PostStat;
import com.kbt.backend.core.post.infrastructure.PostRepository;
import com.kbt.backend.core.user.application.UserQueryService;
import com.kbt.backend.core.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommandService {

    private final PostRepository postRepository;
    private final UserQueryService userQueryService;

    public Post create(
            final String userId,
            final String title,
            final String content,
            final String imageUrl
    ) {
        final User user = userQueryService.findActiveUser(userId);

        final Post post = Post.builder()
                .id(UuidGenerator.generate())
                .userId(userId)
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .likeCount(0L)
                .commentCount(0L)
                .viewCount(0L)
                .build();

        final Long nextPostId = postRepository.findMaxId().orElse(0L) + 1;
        post.assignId(nextPostId);
        post.assignUserId(user.idLong());

        final Post savedPost = postRepository.save(post);

        final Long nextStatId2 = postRepository.findMaxPostStatId2().orElse(0L) + 1;
        final PostStat stat = PostStat.builder()
                .postId(savedPost.idLong())
                .id2(nextStatId2)
                .likeCount(0L)
                .commentCount(0L)
                .viewCount(0L)
                .build();

        savedPost.assignStat(stat);
        return postRepository.save(savedPost);
    }

    public Post increaseLikeCount(final Post post) {
        postRepository.incrementLikeCount(post.idLong());
        return postRepository.findActiveById(post.id())
                .orElse(post);
    }

    public Post decreaseLikeCount(final Post post) {
        postRepository.decrementLikeCount(post.idLong());
        return postRepository.findActiveById(post.id())
                .orElse(post);
    }

    public void increaseCommentCount(final Post post) {
        postRepository.incrementCommentCount(post.idLong());
    }

    public void decreaseCommentCount(final Post post) {
        postRepository.decrementCommentCount(post.idLong());
    }

    public Post increaseViewCount(final Post post) {
        postRepository.incrementViewCount(post.idLong());
        return postRepository.findActiveById(post.id())
                .orElse(post);
    }

    public Post edit(
            final String userId,
            final Post post,
            final String title,
            final String content,
            final String imageUrl
    ) {
        validateWriter(post, userId);

        final PostEditor editor = PostEditor.builder()
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .build();

        post.edit(editor);
        return postRepository.save(post);
    }

    public void delete(
            final String userId,
            final Post post
    ) {
        validateWriter(post, userId);

        post.delete();
        postRepository.save(post);
    }

    private void validateWriter(
            final Post post,
            final String userId
    ) {
        if (!post.isWrittenBy(userId)) {
            throw new ApiException(ErrorType.FORBIDDEN);
        }
    }
}
