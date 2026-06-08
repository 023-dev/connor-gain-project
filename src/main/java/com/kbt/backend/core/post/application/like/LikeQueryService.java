package com.kbt.backend.core.post.application.like;

import com.kbt.backend.core.post.infrastructure.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeQueryService {

    private final LikeRepository likeRepository;

    public long countByPostId(final String postId) {
        return likeRepository.countActiveByPostId(postId);
    }

}
