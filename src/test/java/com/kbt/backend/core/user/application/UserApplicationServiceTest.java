package com.kbt.backend.core.user.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.auth.application.AuthApplicationService;
import com.kbt.backend.core.user.application.dto.UserSignupResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserApplicationServiceTest {

    @Autowired
    private UserApplicationService userService;

    @Autowired
    private AuthApplicationService authService;

    @Test
    void signupReturnsUuidString() {
        final String suffix = suffix("uuid");
        final UserSignupResponse response = userService.signup(
                email(suffix),
                "test1234",
                suffix,
                "https://image.kr/img.jpg"
        );

        assertThat(UUID.fromString(response.userId())).isNotNull();
    }

    @Test
    void signupRejectsDuplicateEmail() {
        final String suffix = suffix("duplicate-email");
        final String email = email(suffix);
        userService.signup(email, "test1234", suffix, "https://image.kr/img.jpg");

        assertThatThrownBy(() -> userService.signup(
                email,
                "test1234",
                "nickname-" + UUID.randomUUID(),
                "https://image.kr/img.jpg"
        ))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorType.DUPLICATE_EMAIL));
    }

    @Test
    void signupRejectsDuplicateNickname() {
        final String suffix = suffix("duplicate-nickname");
        userService.signup(email(suffix), "test1234", suffix, "https://image.kr/img.jpg");

        assertThatThrownBy(() -> userService.signup(
                "email-" + UUID.randomUUID() + "@startupcode.kr",
                "test1234",
                suffix,
                "https://image.kr/img.jpg"
        ))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorType.DUPLICATE_NICKNAME));
    }

    @Test
    void concurrentSignupAllowsOnlyOneActiveUserWithSameEmail() throws Exception {
        final String suffix = suffix("concurrent-email");
        final String email = email(suffix);
        final int requestCount = 20;
        final ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
        final CountDownLatch startSignal = new CountDownLatch(1);

        final List<Future<Boolean>> results = IntStream.range(0, requestCount)
                .mapToObj(index -> executorService.submit(() -> {
                    startSignal.await();
                    try {
                        userService.signup(
                                email,
                                "test1234",
                                suffix + "-" + index,
                                "https://image.kr/img.jpg"
                        );
                        return true;
                    } catch (ApiException exception) {
                        assertThat(exception.errorCode()).isEqualTo(ErrorType.DUPLICATE_EMAIL);
                        return false;
                    }
                }))
                .toList();

        startSignal.countDown();

        final long successCount = results.stream()
                .filter(result -> {
                    try {
                        return result.get();
                    } catch (Exception exception) {
                        throw new IllegalStateException(exception);
                    }
                })
                .count();

        executorService.shutdownNow();

        assertThat(successCount).isEqualTo(1);
    }

    @Test
    void signinRejectsInvalidCredentials() {
        final String suffix = suffix("invalid-credentials");
        final String email = email(suffix);
        userService.signup(email, "test1234", suffix, "https://image.kr/img.jpg");

        assertThatThrownBy(() -> authService.login(email, "wrong-password"))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorType.INVALID_CREDENTIALS));
    }

    @Test
    void updatePasswordAndDeleteFlow() {
        final String suffix = suffix("flow");
        final String email = email(suffix);
        final String userId = userService.signup(
                email,
                "test1234",
                suffix,
                "https://image.kr/img.jpg"
        ).userId();

        userService.update(userId, "updated-" + UUID.randomUUID(), "https://image.kr/updated.jpg");
        userService.updatePassword(userId, "test1234", "new-password");

        assertThat(authService.login(email, "new-password").userId())
                .isEqualTo(userId);

        userService.delete(userId);

        assertThatThrownBy(() -> userService.me(userId))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorType.USER_NOT_FOUND));
    }

    @Test
    void signinFindsActiveUserWhenDeletedUserHasSameEmail() {
        final String firstSuffix = suffix("reuse-email");
        final String firstEmail = email(firstSuffix);
        final String firstUserId = userService.signup(
                firstEmail,
                "test1234",
                firstSuffix,
                "https://image.kr/img.jpg"
        ).userId();
        userService.delete(firstUserId);

        final String secondNickname = "reused-nickname-" + UUID.randomUUID();
        final String secondUserId = userService.signup(
                firstEmail,
                "test1234",
                secondNickname,
                "https://image.kr/img.jpg"
        ).userId();

        assertThat(authService.login(firstEmail, "test1234").userId())
                .isEqualTo(secondUserId);
    }

    private String suffix(final String name) {
        return name + "-" + UUID.randomUUID();
    }

    private String email(final String suffix) {
        return suffix + "@startupcode.kr";
    }
}
