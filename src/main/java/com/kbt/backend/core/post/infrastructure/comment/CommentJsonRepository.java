package com.kbt.backend.core.post.infrastructure.comment;

import com.kbt.backend.core.post.domain.comment.Comment;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class CommentJsonRepository implements CommentRepository {

    private final ConcurrentMap<String, Comment> comments = new ConcurrentHashMap<>();

    @Override
    public Comment save(final Comment comment) {
        comments.put(comment.id(), comment);
        return comment;
    }

    @Override
    public Optional<Comment> findActiveById(final String id) {
        return Optional.ofNullable(comments.get(id))
                .filter(this::isActive);
    }

    @Override
    public List<Comment> findAllActiveByPostId(final String postId) {
        return comments.values().stream()
                .filter(this::isActive)
                .filter(comment -> comment.postId().equals(postId))
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .toList();
    }

    private boolean isActive(final Comment comment) {
        return !comment.deleted();
    }

}
