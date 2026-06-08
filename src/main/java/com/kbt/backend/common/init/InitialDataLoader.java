package com.kbt.backend.common.init;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbt.backend.common.init.seed.CommentSeedData;
import com.kbt.backend.common.init.seed.PostSeedData;
import com.kbt.backend.common.init.seed.UserSeedData;
import com.kbt.backend.core.post.infrastructure.PostRepository;
import com.kbt.backend.core.post.infrastructure.comment.CommentRepository;
import com.kbt.backend.core.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InitialDataLoader implements ApplicationRunner {

    private static final String USER_SEED_PATH = "data/users.json";
    private static final String POST_SEED_PATH = "data/posts.json";
    private static final String COMMENT_SEED_PATH = "data/comments.json";

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    public void run(final ApplicationArguments args) {
        loadUserSeedDataIntoRepository();
        loadPostSeedDataIntoRepository();
        loadCommentSeedDataIntoRepository();
    }

    private void loadUserSeedDataIntoRepository() {
        readJsonSeedData(USER_SEED_PATH, UserSeedData.class).stream()
                .map(UserSeedData::toUser)
                .forEach(userRepository::save);
    }

    private void loadPostSeedDataIntoRepository() {
        readJsonSeedData(POST_SEED_PATH, PostSeedData.class).stream()
                .map(PostSeedData::toPost)
                .forEach(postRepository::save);
    }

    private void loadCommentSeedDataIntoRepository() {
        readJsonSeedData(COMMENT_SEED_PATH, CommentSeedData.class).stream()
                .map(CommentSeedData::toComment)
                .forEach(commentRepository::save);
    }

    private <T> List<T> readJsonSeedData(
            final String seedPath,
            final Class<T> seedType
    ) {
        final ClassPathResource resource = new ClassPathResource(seedPath);
        if (!resource.exists()) {
            return List.of();
        }

        final JavaType seedListType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, seedType);

        try (final InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, seedListType);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load seed data: " + seedPath, exception);
        }
    }
}
