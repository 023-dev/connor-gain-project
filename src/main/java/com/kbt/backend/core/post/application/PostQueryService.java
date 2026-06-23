package com.kbt.backend.core.post.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.post.infrastructure.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostRepository postRepository;

    public Post findOne(final String postId) {
        return postRepository.findActiveById(postId)
                .orElseThrow(() -> new ApiException(ErrorType.POST_NOT_FOUND));
    }
}
