package com.kbt.backend.core.post.application.like;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.common.utils.UuidGenerator;
import com.kbt.backend.core.post.domain.like.Like;
import com.kbt.backend.core.post.infrastructure.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeCommandService {

    private final LikeRepository likeRepository;

    public Like like(
            final String userId,
            final String postId
    ) {
        if (likeRepository.findActiveByPostIdAndUserId(postId, userId).isPresent()) {
            throw new ApiException(ErrorType.ALREADY_LIKED);
        }

        final Like like = likeRepository.findByPostIdAndUserId(postId, userId)
                .orElseGet(() -> Like.builder()
                        .id(UuidGenerator.generate())
                        .postId(postId)
                        .userId(userId)
                        .build());

        like.activate();
        return likeRepository.save(like);
    }

    public void unlike(
            final String userId,
            final String postId
    ) {
        final Like like = likeRepository.findActiveByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_LIKED));

        like.delete();
        likeRepository.save(like);
    }
}
