package com.kbt.backend.core.user.infrastructure;

import com.kbt.backend.core.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKey(String key);
    Optional<User> findByKeyAndDeletedFalse(String key);
    Optional<User> findByEmailAndDeletedFalse(String email);
    Optional<User> findByNicknameAndDeletedFalse(String nickname);
}
