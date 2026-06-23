package com.kbt.backend.core.user.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.user.domain.User;
import com.kbt.backend.core.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;

    public User me(final String userId) {
        return findActiveUser(userId);
    }

    public User findActiveUser(final String userId) {
        return userRepository.findByKeyAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ApiException(ErrorType.USER_NOT_FOUND));
    }

    public User findUser(final String userId) {
        return userRepository.findByKey(userId)
                .orElseThrow(() -> new ApiException(ErrorType.USER_NOT_FOUND));
    }
}
