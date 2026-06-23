package com.kbt.backend.core.post.infrastructure.comment;

import com.kbt.backend.core.post.domain.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c where c.key = :id and c.deletedAt is null")
    Optional<Comment> findActiveById(@Param("id") final String id);

    @Query("select c from Comment c where c.postKey = :postId and c.deletedAt is null")
    List<Comment> findAllActiveByPostId(@Param("postId") final String postId);
}
