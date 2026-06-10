package com.kbt.backend.core.post.infrastructure.like;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.post.domain.like.Like;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class LikeJsonRepository implements LikeRepository {

    private final ConcurrentMap<String, Like> likes = new ConcurrentHashMap<>();

    @Override
    public synchronized Like save(final Like like) {
        validateUniqueActiveLike(like);
        likes.put(like.id(), like);
        return like;
    }

    @Override
    public synchronized Optional<Like> findByPostIdAndUserId(
            final String postId,
            final String userId
    ) {
        return likes.values().stream()
                .filter(like -> like.postId().equals(postId))
                .filter(like -> like.userId().equals(userId))
                .findFirst();
    }

    @Override
    public synchronized Optional<Like> findActiveByPostIdAndUserId(
            final String postId,
            final String userId
    ) {
        return findByPostIdAndUserId(postId, userId)
                .filter(this::isActive);
    }

    @Override
    public synchronized boolean existsActiveByPostIdAndUserId(
            final String postId,
            final String userId
    ) {
        return findActiveByPostIdAndUserId(postId, userId).isPresent();
    }

    @Override
    public synchronized long countActiveByPostId(final String postId) {
        return likes.values().stream()
                .filter(this::isActive)
                .filter(like -> like.postId().equals(postId))
                .count();
    }

    private void validateUniqueActiveLike(final Like like) {
        if (like.deleted()) {
            return;
        }

        likes.values().stream()
                .filter(this::isActive)
                .filter(savedLike -> !savedLike.id().equals(like.id()))
                .filter(savedLike -> savedLike.postId().equals(like.postId()) && savedLike.userId().equals(like.userId()))
                .findFirst()
                .ifPresent(savedLike -> {
                    throw new ApiException(ErrorType.ALREADY_LIKED);
                });
    }

    private boolean isActive(final Like like) {
        return !like.deleted();
    }
}
