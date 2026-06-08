package com.kbt.backend.core.user.infrastructure;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class UserJsonRepository implements UserRepository {

    private final ConcurrentMap<String, User> users = new ConcurrentHashMap<>();

    @Override
    public synchronized User save(final User user) {
        validateUniqueActiveEmail(user);
        validateUniqueActiveNickname(user);
        users.put(user.id(), user);
        return user;
    }

    @Override
    public Optional<User> findById(final String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findActiveById(final String id) {
        return findById(id)
                .filter(this::isActive);
    }

    @Override
    public Optional<User> findActiveByEmail(final String email) {
        return users.values().stream()
                .filter(user -> user.email().equals(email))
                .filter(this::isActive)
                .findFirst();
    }

    @Override
    public Optional<User> findActiveByNickname(final String nickname) {
        return users.values().stream()
                .filter(user -> user.nickname().equals(nickname))
                .filter(this::isActive)
                .findFirst();
    }

    private boolean isActive(final User user) {
        return !user.deleted();
    }

    private void validateUniqueActiveEmail(final User user) {
        if (user.deleted()) {
            return;
        }

        users.values().stream()
                .filter(this::isActive)
                .filter(savedUser -> !savedUser.id().equals(user.id()))
                .filter(savedUser -> savedUser.email().equals(user.email()))
                .findFirst()
                .ifPresent(savedUser -> {
                    throw new ApiException(ErrorType.DUPLICATE_EMAIL);
                });
    }

    private void validateUniqueActiveNickname(final User user) {
        if (user.deleted()) {
            return;
        }

        users.values().stream()
                .filter(this::isActive)
                .filter(savedUser -> !savedUser.id().equals(user.id()))
                .filter(savedUser -> savedUser.nickname().equals(user.nickname()))
                .findFirst()
                .ifPresent(savedUser -> {
                    throw new ApiException(ErrorType.DUPLICATE_NICKNAME);
                });
    }

}
