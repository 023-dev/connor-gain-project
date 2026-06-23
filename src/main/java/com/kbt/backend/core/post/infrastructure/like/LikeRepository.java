package com.kbt.backend.core.post.infrastructure.like;

import com.kbt.backend.core.post.domain.like.Like;
import com.kbt.backend.core.post.domain.like.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    @Query("select l from Like l where l.postKey = :postId and l.userKey = :userId")
    Optional<Like> findByPostIdAndUserId(
            @Param("postId") final String postId,
            @Param("userId") final String userId
    );

    @Query("select l from Like l where l.postKey = :postId and l.userKey = :userId and l.deleted = false")
    Optional<Like> findActiveByPostIdAndUserId(
            @Param("postId") final String postId,
            @Param("userId") final String userId
    );

    @Query("select count(l) > 0 from Like l where l.postKey = :postId and l.userKey = :userId and l.deleted = false")
    boolean existsActiveByPostIdAndUserId(
            @Param("postId") final String postId,
            @Param("userId") final String userId
    );

    @Query("select count(l) from Like l where l.postKey = :postId and l.deleted = false")
    long countActiveByPostId(@Param("postId") final String postId);
}
