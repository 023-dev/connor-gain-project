package com.kbt.backend.core.user.infrastructure;

import com.kbt.backend.core.user.domain.DeletedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeletedUserRepository extends JpaRepository<DeletedUser, Long> {
}
