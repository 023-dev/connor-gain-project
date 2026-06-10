package com.kbt.backend.core.user.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.common.utils.UuidGenerator;
import com.kbt.backend.core.user.domain.User;
import com.kbt.backend.core.user.domain.UserEditor;
import com.kbt.backend.core.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;

    public User signup(
            final String email,
            final String password,
            final String nickname,
            final String profileImage
    ) {
        validateDuplicateEmail(email);
        validateDuplicateNickname(nickname);

        final User user = User.builder()
                .id(UuidGenerator.generate())
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
        final User user = userRepository.findActiveByEmail(email)
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
        user.delete();
        userRepository.save(user);
    }

    private void validateDuplicateEmail(final String email) {
        userRepository.findActiveByEmail(email)
                .ifPresent(user -> {
                    throw new ApiException(ErrorType.DUPLICATE_EMAIL);
                });
    }

    private void validateDuplicateNickname(final String nickname) {
        userRepository.findActiveByNickname(nickname)
                .ifPresent(user -> {
                    throw new ApiException(ErrorType.DUPLICATE_NICKNAME);
                });
    }

    private User findActiveUser(final String userId) {
        return userRepository.findActiveById(userId)
                .orElseThrow(() -> new ApiException(ErrorType.USER_NOT_FOUND));
    }
}
