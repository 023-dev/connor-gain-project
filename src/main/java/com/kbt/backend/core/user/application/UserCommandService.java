package com.kbt.backend.core.user.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.common.utils.UuidGenerator;
import com.kbt.backend.core.user.domain.DeletedUser;
import com.kbt.backend.core.user.domain.User;
import com.kbt.backend.core.user.domain.UserEditor;
import com.kbt.backend.core.user.infrastructure.DeletedUserRepository;
import com.kbt.backend.core.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;
    private final DeletedUserRepository deletedUserRepository;

    public synchronized User signup(
            final String email,
            final String password,
            final String nickname,
            final String profileImage
    ) {
        validateDuplicateEmail(email);
        validateDuplicateNickname(nickname);

        final User user = User.builder()
                .key(UuidGenerator.generate())
                .email(email)
                .password(password)
                .nickname(nickname)
                .profileImage(profileImage)
                .build();

        return userRepository.save(user);
    }

    public User signin(
            final String email,
            final String password
    ) {
        final User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ApiException(ErrorType.INVALID_CREDENTIALS));

        if (!user.password().equals(password)) {
            throw new ApiException(ErrorType.INVALID_CREDENTIALS);
        }

        return user;
    }

    public User update(
            final User user,
            final String nickname,
            final String profileImage
    ) {
        if (StringUtils.hasText(nickname) && !user.nickname().equals(nickname)) {
            validateDuplicateNickname(nickname);
        }

        final UserEditor editor = UserEditor.builder()
                .nickname(nickname)
                .profileImage(profileImage)
                .build();

        user.edit(editor);
        return userRepository.save(user);
    }

    public void updatePassword(
            final String userId,
            final String currentPassword,
            final String newPassword
    ) {
        final User user = findActiveUser(userId);
        if (!user.password().equals(currentPassword)) {
            throw new ApiException(ErrorType.INVALID_CREDENTIALS);
        }
        user.updatePassword(newPassword);
        userRepository.save(user);
    }

    public void signout(final String userId) {
        findActiveUser(userId);
    }

    public void delete(final String userId) {
        final User user = findActiveUser(userId);

        // 1. 탈퇴 유저 백업 정보 저장
        final DeletedUser deletedUser = DeletedUser.builder()
                .userKey(user.id())
                .email(user.email())
                .nickname(user.nickname())
                .build();
        deletedUserRepository.save(deletedUser);

        // 2. 실서비스 테이블 유저 정보 마스킹/익명화 및 탈퇴 처리
        final String dummyEmail = "deleted_" + UUID.randomUUID().toString() + "@kbt.com";
        user.delete(dummyEmail);
        userRepository.save(user);
    }

    private void validateDuplicateEmail(final String email) {
        userRepository.findByEmailAndDeletedFalse(email)
                .ifPresent(user -> {
                    throw new ApiException(ErrorType.DUPLICATE_EMAIL);
                });
    }

    private void validateDuplicateNickname(final String nickname) {
        userRepository.findByNicknameAndDeletedFalse(nickname)
                .ifPresent(user -> {
                    throw new ApiException(ErrorType.DUPLICATE_NICKNAME);
                });
    }

    private User findActiveUser(final String userId) {
        return userRepository.findByKeyAndDeletedFalse(userId)
                .orElseThrow(() -> new ApiException(ErrorType.USER_NOT_FOUND));
    }
}
