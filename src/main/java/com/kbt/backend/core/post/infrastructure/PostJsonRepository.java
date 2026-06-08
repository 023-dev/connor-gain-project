package com.kbt.backend.core.post.infrastructure;

import com.kbt.backend.core.post.domain.Post;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class PostJsonRepository implements PostRepository {

    private final ConcurrentMap<String, Post> posts = new ConcurrentHashMap<>();

    @Override
    public Post save(final Post post) {
        posts.put(post.id(), post);
        return post;
    }

    @Override
    public Optional<Post> findActiveById(final String id) {
        return Optional.ofNullable(posts.get(id))
                .filter(this::isActive);
    }

    @Override
    public List<Post> findAllActive() {
        return posts.values().stream()
                .filter(this::isActive)
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .toList();
    }

    private boolean isActive(final Post post) {
        return !post.deleted();
    }

}
