package com.kbt.backend.core.post.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.post.infrastructure.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;

    public Post findOne(final String postId) {
        return postRepository.findActiveById(postId)
                .orElseThrow(() -> new ApiException(ErrorType.POST_NOT_FOUND));
    }

    public List<Post> findAll() {
        return postRepository.findAllActive();
    }
}
