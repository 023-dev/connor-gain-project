package com.kbt.backend.core.post.infrastructure.comment;

import com.kbt.backend.core.post.domain.comment.DeletedComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeletedCommentRepository extends JpaRepository<DeletedComment, Long> {
}
