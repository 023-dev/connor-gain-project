package com.kbt.backend.core.post.infrastructure;

import com.kbt.backend.core.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Query("select p from Post p where p.key = :key")
    Optional<Post> findActiveById(@Param("key") final String key);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update PostStat ps set ps.viewCount = ps.viewCount + 1 where ps.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update PostStat ps set ps.likeCount = ps.likeCount + 1 where ps.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update PostStat ps set ps.likeCount = ps.likeCount - 1 where ps.id = :postId and ps.likeCount > 0")
    void decrementLikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update PostStat ps set ps.commentCount = ps.commentCount + 1 where ps.id = :postId")
    void incrementCommentCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update PostStat ps set ps.commentCount = ps.commentCount - 1 where ps.id = :postId and ps.commentCount > 0")
    void decrementCommentCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update PostStat ps set ps.viewCount = case when ps.viewCount + :delta < 0 then 0 else ps.viewCount + :delta end where ps.id = :postId")
    void updateViewCount(
            @Param("postId") Long postId,
            @Param("delta") long delta
    );

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update PostStat ps set ps.likeCount = case when ps.likeCount + :delta < 0 then 0 else ps.likeCount + :delta end where ps.id = :postId")
    void updateLikeCount(
            @Param("postId") Long postId,
            @Param("delta") long delta
    );

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update PostStat ps set ps.commentCount = case when ps.commentCount + :delta < 0 then 0 else ps.commentCount + :delta end where ps.id = :postId")
    void updateCommentCount(
            @Param("postId") Long postId,
            @Param("delta") long delta
    );
}
