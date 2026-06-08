package com.kbt.backend.core.post.application.comment;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.post.application.comment.dto.CommentCreateResponse;
import com.kbt.backend.core.post.application.comment.dto.CommentResponse;
import com.kbt.backend.core.post.application.comment.dto.CommentUpdateResponse;
import com.kbt.backend.core.post.application.comment.dto.CommentsResponse;
import com.kbt.backend.core.post.application.PostQueryService;
import com.kbt.backend.core.post.domain.comment.Comment;
import com.kbt.backend.core.user.application.UserQueryService;
import com.kbt.backend.core.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentApplicationService {

    private final CommentCommandService commentCommandService;
    private final CommentQueryService commentQueryService;
    private final PostQueryService postQueryService;
    private final UserQueryService userQueryService;

    public CommentCreateResponse create(
            final String userId,
            final String postId,
            final String content
    ) {
        final User user = userQueryService.findActiveUser(userId);
        postQueryService.findOne(postId);

        final Comment comment = commentCommandService.create(user.id(), postId, content);
        return new CommentCreateResponse(comment.id());
    }

    public CommentResponse findOne(
            final String postId,
            final String commentId
    ) {
        postQueryService.findOne(postId);
        final Comment comment = commentQueryService.findOne(commentId);
        validateBelongsToPost(comment, postId);

        final User user = userQueryService.findActiveUser(comment.userId());
        return CommentResponse.from(comment, user);
    }

    public CommentsResponse findAllByPostId(final String postId) {
        postQueryService.findOne(postId);

        final List<CommentResponse> comments = commentQueryService.findAllByPostId(postId).stream()
                .map(comment -> CommentResponse.from(comment, userQueryService.findActiveUser(comment.userId())))
                .toList();

        return new CommentsResponse(comments);
    }

    public CommentUpdateResponse edit(
            final String userId,
            final String postId,
            final String commentId,
            final String content
    ) {
        validateHasEditValue(content);

        final User user = userQueryService.findActiveUser(userId);
        postQueryService.findOne(postId);
        final Comment comment = commentQueryService.findOne(commentId);
        validateBelongsToPost(comment, postId);

        final Comment editedComment = commentCommandService.edit(user.id(), comment, content);
        return CommentUpdateResponse.from(editedComment);
    }

    public void delete(
            final String userId,
            final String postId,
            final String commentId
    ) {
        final User user = userQueryService.findActiveUser(userId);
        postQueryService.findOne(postId);
        final Comment comment = commentQueryService.findOne(commentId);
        validateBelongsToPost(comment, postId);

        commentCommandService.delete(user.id(), comment);
    }

    private void validateBelongsToPost(
            final Comment comment,
            final String postId
    ) {
        if (!comment.belongsTo(postId)) {
            throw new ApiException(ErrorType.COMMENT_NOT_FOUND);
        }
    }

    private void validateHasEditValue(final String content) {
        if (!StringUtils.hasText(content)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }
}
