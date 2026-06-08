package com.kbt.backend.core.post.infrastructure.comment;

import com.kbt.backend.core.post.domain.comment.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    Comment save(final Comment comment);

    Optional<Comment> findActiveById(final String id);

    List<Comment> findAllActiveByPostId(final String postId);
}
