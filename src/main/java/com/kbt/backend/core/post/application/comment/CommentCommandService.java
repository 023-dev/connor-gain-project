package com.kbt.backend.core.post.application.comment;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.common.utils.UuidGenerator;
import com.kbt.backend.core.post.application.PostQueryService;
import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.post.domain.comment.Comment;
import com.kbt.backend.core.post.domain.comment.CommentEditor;
import com.kbt.backend.core.post.infrastructure.comment.CommentRepository;
import com.kbt.backend.core.user.application.UserQueryService;
import com.kbt.backend.core.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final UserQueryService userQueryService;
    private final PostQueryService postQueryService;

    public Comment create(
            final String userId,
            final String postId,
            final String content
    ) {
        final User user = userQueryService.findActiveUser(userId);
        final Post post = postQueryService.findOne(postId);

        final Comment comment = Comment.builder()
                .id(UuidGenerator.generate())
                .postId(postId)
                .userId(userId)
                .content(content)
                .build();

        comment.assignIds(user.idLong(), post.idLong());

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

        comment.delete();
        commentRepository.saveAndFlush(comment);
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
