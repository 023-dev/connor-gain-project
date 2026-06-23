package com.kbt.backend.core.post.application;

import com.kbt.backend.core.post.domain.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PostCommandServiceTest {

    @Autowired
    private PostCommandService postCommandService;

    @Autowired
    private PostQueryService postQueryService;

    @Autowired
    private com.kbt.backend.core.user.infrastructure.UserRepository userRepository;

    @Test
    void concurrentViewCountIncreaseDoesNotLoseUpdates() throws Exception {
        if (userRepository.findByKeyAndDeletedFalse("concurrent-user").isEmpty()) {
            userRepository.save(com.kbt.backend.core.user.domain.User.builder()
                    .key("concurrent-user")
                    .email("concurrent@kbt.com")
                    .password("password")
                    .nickname("nickname")
                    .build());
        }

        final int requestCount = 50;
        final Post post = postCommandService.create(
                "concurrent-user",
                "title",
                "content",
                "https://image.kr/post.jpg"
        );
        final ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
        final CountDownLatch startSignal = new CountDownLatch(1);

        final List<Future<Void>> results = IntStream.range(0, requestCount)
                .mapToObj(index -> executorService.submit(() -> {
                    startSignal.await();
                    postCommandService.increaseViewCount(post);
                    return (Void) null;
                }))
                .toList();

        startSignal.countDown();
        for (final Future<?> result : results) {
            result.get();
        }
        executorService.shutdownNow();

        assertThat(postQueryService.findOne(post.id()).viewCount()).isEqualTo(requestCount);
    }
}
