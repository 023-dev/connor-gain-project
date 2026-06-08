package com.kbt.backend.core.post.application.comment;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.post.domain.comment.Comment;
import com.kbt.backend.core.post.infrastructure.comment.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepository commentRepository;

    public Comment findOne(final String commentId) {
        return commentRepository.findActiveById(commentId)
                .orElseThrow(() -> new ApiException(ErrorType.COMMENT_NOT_FOUND));
    }

    public List<Comment> findAllByPostId(final String postId) {
        return commentRepository.findAllActiveByPostId(postId);
    }
}
