package com.kbt.backend.core.post.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.common.utils.UuidGenerator;
import com.kbt.backend.core.post.domain.DeletedPost;
import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.post.domain.PostEditor;
import com.kbt.backend.core.post.domain.PostStat;
import com.kbt.backend.core.post.infrastructure.DeletedPostRepository;
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
    private final DeletedPostRepository deletedPostRepository;
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

        post.assignUserId(user.idLong());

        final Post savedPost = postRepository.save(post);

        final PostStat stat = PostStat.builder()
                .postId(savedPost.idLong())
                .id2(savedPost.userIdLong())
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

        // 1. 격리 백업 테이블에 저장
        final DeletedPost deletedPost = DeletedPost.builder()
                .postId(post.idLong())
                .userId(post.userIdLong())
                .postKey(post.id())
                .userKey(post.userId())
                .title(post.title())
                .content(post.content())
                .imageUrl(post.imageUrl())
                .build();
        deletedPostRepository.save(deletedPost);

        // 2. 실서비스 테이블 유저 게시글 삭제 및 익명화 자동화 호출
        postRepository.delete(post);
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
