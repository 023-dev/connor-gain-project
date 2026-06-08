package com.kbt.backend.core.post.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.common.utils.UuidGenerator;
import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.post.domain.PostEditor;
import com.kbt.backend.core.post.infrastructure.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;

    public Post create(
            final String userId,
            final String title,
            final String content,
            final String imageUrl
    ) {
        final Post post = Post.builder()
                .id(UuidGenerator.generate())
                .userId(userId)
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .build();

        return postRepository.save(post);
    }

    public Post edit(
            final String userId,
            final Post post,
            final String title,
            final String content,
            final String imageUrl
    ) {
        validateWriter(post, userId);

        final PostEditor editor = PostEditor.builder()
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .build();

        post.edit(editor);
        return postRepository.save(post);
    }

    public void delete(
            final String userId,
            final Post post
    ) {
        validateWriter(post, userId);

        post.delete();
        postRepository.save(post);
    }

    private void validateWriter(
            final Post post,
            final String userId
    ) {
        if (!post.isWrittenBy(userId)) {
            throw new ApiException(ErrorType.FORBIDDEN);
        }
    }
}
