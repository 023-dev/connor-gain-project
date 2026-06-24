package com.kbt.backend.core.post.application.comment;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.common.utils.UuidGenerator;
import com.kbt.backend.core.post.application.PostQueryService;
import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.post.domain.comment.Comment;
import com.kbt.backend.core.post.domain.comment.CommentEditor;
import com.kbt.backend.core.post.domain.comment.DeletedComment;
import com.kbt.backend.core.post.infrastructure.comment.DeletedCommentRepository;
import com.kbt.backend.core.post.infrastructure.comment.CommentRepository;
import com.kbt.backend.core.user.application.UserQueryService;
import com.kbt.backend.core.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final DeletedCommentRepository deletedCommentRepository;
    private final UserQueryService userQueryService;
    private final PostQueryService postQueryService;

    public Comment create(
            final String userId,
            final String postId,
            final String content
    ) {
        final User user = userQueryService.findActiveUser(userId);
        final Post post = postQueryService.findOne(postId);

        final Comment comment = Comment.create(userId, user.idLong(), postId, post.idLong(), content);

        return commentRepository.saveAndFlush(comment);
    }

    public Comment edit(
            final String userId,
            final Comment comment,
            final String content
    ) {
        validateWriter(comment, userId);

        final CommentEditor editor = CommentEditor.builder()
                .content(content)
                .build();

        comment.edit(editor);
        return commentRepository.saveAndFlush(comment);
    }

    public void delete(
            final String userId,
            final Comment comment
    ) {
        validateWriter(comment, userId);

        // 1. 격리 백업 테이블에 저장
        final DeletedComment deletedComment = DeletedComment.from(comment);
        deletedCommentRepository.save(deletedComment);

        // 2. 실서비스 테이블 댓글 삭제 및 마스킹 자동화 호출
        commentRepository.delete(comment);
        commentRepository.flush();
    }

    private void validateWriter(
            final Comment comment,
            final String userId
    ) {
        if (!comment.isWrittenBy(userId)) {
            throw new ApiException(ErrorType.FORBIDDEN);
        }
    }
}
