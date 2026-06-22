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

import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.post.domain.PostStat;
import com.kbt.backend.core.user.domain.User;

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
                .forEach(seed -> {
                    final User user = userRepository.findByKeyAndDeletedFalse(seed.userId())
                            .orElseThrow(() -> new IllegalStateException("User not found for seed post: " + seed.userId()));

                    final Post post = Post.builder()
                            .id(seed.id())
                            .userId(seed.userId())
                            .title(seed.title())
                            .content(seed.content())
                            .imageUrl(seed.imageUrl())
                            .likeCount(0L)
                            .commentCount(0L)
                            .viewCount(0L)
                            .deleted(seed.deleted())
                            .build();

                    final Long nextPostId = postRepository.findMaxId().orElse(0L) + 1;
                    post.assignId(nextPostId);
                    post.assignUserId(user.idLong());
                    final Post savedPost = postRepository.save(post);

                    final Long nextStatId2 = postRepository.findMaxPostStatId2().orElse(0L) + 1;
                    final PostStat stat = PostStat.builder()
                            .postId(savedPost.idLong())
                            .id2(nextStatId2)
                            .likeCount(0L)
                            .commentCount(0L)
                            .viewCount(0L)
                            .build();

                    savedPost.assignStat(stat);
                    postRepository.save(savedPost);
                });
    }

    private void loadCommentSeedDataIntoRepository() {
        readJsonSeedData(COMMENT_SEED_PATH, CommentSeedData.class).stream()
                .map(CommentSeedData::toComment)
                .forEach(comment -> {
                    commentRepository.save(comment);
                    postRepository.findActiveById(comment.postId())
                            .ifPresent(post -> {
                                postRepository.incrementCommentCount(post.idLong());
                            });
                });
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
            throw new IllegalStateException("데이터를 초기화하는 과정이 실패했습니다." + seedPath, exception);
        }
    }
}
