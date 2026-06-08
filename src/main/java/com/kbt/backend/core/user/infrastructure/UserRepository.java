package com.kbt.backend.core.user.infrastructure;

import com.kbt.backend.core.user.domain.User;

import java.util.Optional;

public interface UserRepository {
    User save(final User user);
    Optional<User> findById(final String id);
    Optional<User> findActiveById(final String id);
    Optional<User> findActiveByEmail(final String email);
    Optional<User> findActiveByNickname(final String nickname);
}
