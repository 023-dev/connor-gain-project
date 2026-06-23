package com.kbt.backend.core.post.infrastructure;

import com.kbt.backend.core.post.domain.DeletedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeletedPostRepository extends JpaRepository<DeletedPost, Long> {
}
