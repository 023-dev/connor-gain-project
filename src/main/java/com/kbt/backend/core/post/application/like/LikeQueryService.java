package com.kbt.backend.core.post.application.like;

import com.kbt.backend.core.post.infrastructure.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeQueryService {

    private final LikeRepository likeRepository;

    public long countByPostId(final String postId) {
        return likeRepository.countActiveByPostId(postId);
    }

    public boolean isLikedByUser(
            final String postId,
            final Optional<String> userId
    ) {
        return userId.filter(StringUtils::hasText)
                .map(id -> likeRepository.existsActiveByPostIdAndUserId(postId, id))
                .orElse(false);
    }

}
