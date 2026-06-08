package com.kbt.backend.core.user.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.user.domain.User;
import com.kbt.backend.core.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    public User me(final String userId) {
        return findActiveUser(userId);
    }

    public User findActiveUser(final String userId) {
        return userRepository.findActiveById(userId)
                .orElseThrow(() -> new ApiException(ErrorType.USER_NOT_FOUND));
    }
}
