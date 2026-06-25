package com.kbt.backend.core.post.application;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kbt.backend.core.post.infrastructure.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class CaffeinePostStatBuffer implements PostStatBuffer {

    private final PostRepository postRepository;
    private final Cache<Long, PostStatDeltas> cache = Caffeine.newBuilder().build();

    @Override
    public void incrementLikeCount(final Long postId) {
        cache.get(postId, k -> new PostStatDeltas()).addLike(1);
    }

    @Override
    public void decrementLikeCount(final Long postId) {
        cache.get(postId, k -> new PostStatDeltas()).addLike(-1);
    }

    @Override
    public void incrementCommentCount(final Long postId) {
        cache.get(postId, k -> new PostStatDeltas()).addComment(1);
    }

    @Override
    public void decrementCommentCount(final Long postId) {
        cache.get(postId, k -> new PostStatDeltas()).addComment(-1);
    }

    @Override
    public void incrementViewCount(final Long postId) {
        cache.get(postId, k -> new PostStatDeltas()).addView(1);
    }

    @Override
    public long pendingLikeDelta(final Long postId) {
        final PostStatDeltas deltas = cache.getIfPresent(postId);
        return deltas == null ? 0L : deltas.getLikeDelta();
    }

    @Override
    public long pendingCommentDelta(final Long postId) {
        final PostStatDeltas deltas = cache.getIfPresent(postId);
        return deltas == null ? 0L : deltas.getCommentDelta();
    }

    @Override
    public long pendingViewDelta(final Long postId) {
        final PostStatDeltas deltas = cache.getIfPresent(postId);
        return deltas == null ? 0L : deltas.getViewDelta();
    }

    @Override
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void flush() {
        final ConcurrentMap<Long, PostStatDeltas> map = cache.asMap();
        if (map.isEmpty()) {
            return;
        }

        for (final Long postId : new ArrayList<>(map.keySet())) {
            final PostStatDeltas deltas = map.remove(postId);
            if (deltas != null && !deltas.isEmpty()) {
                if (deltas.getViewDelta() != 0) {
                    postRepository.updateViewCount(postId, deltas.getViewDelta());
                }
                if (deltas.getLikeDelta() != 0) {
                    postRepository.updateLikeCount(postId, deltas.getLikeDelta());
                }
                if (deltas.getCommentDelta() != 0) {
                    postRepository.updateCommentCount(postId, deltas.getCommentDelta());
                }
            }
        }
    }
}
