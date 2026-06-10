package com.kbt.backend.core.post.infrastructure;

import com.kbt.backend.core.post.domain.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(final Post post);

    Optional<Post> findActiveById(final String id);

    List<Post> findAllActive();

    List<Post> findAllActiveByCursor(final List<String> cursorValues, final int limit);
}
