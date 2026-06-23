package com.kbt.backend.core.post.infrastructure;

import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.post.domain.PostId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface PostRepository extends JpaRepository<Post, PostId> {

    @Query("select p from Post p where p.key = :key and p.deleted = false")
    Optional<Post> findActiveById(@Param("key") final String key);

    @Query("select p from Post p where p.deleted = false order by p.createdAt desc, p.key desc")
    List<Post> findAllActive();

    @Query("select max(p.id) from Post p")
    Optional<Long> findMaxId();

    @Query("select max(ps.userId) from PostStat ps")
    Optional<Long> findMaxPostStatId2();

    @Query("select p from Post p where p.deleted = false and " +
           "(:createdAt is null or p.createdAt < :createdAt or (p.createdAt = :createdAt and p.key < :key)) " +
           "order by p.createdAt desc, p.key desc")
    List<Post> findActiveByCursor(
            @Param("createdAt") LocalDateTime createdAt,
            @Param("key") String key,
            Pageable pageable
    );

    default List<Post> findAllActiveByCursor(final List<String> cursorValues, final int limit) {
        if (cursorValues == null) {
            return findActiveByCursor(null, null, PageRequest.of(0, limit));
        }
        final LocalDateTime cursorCreatedAt = LocalDateTime.parse(cursorValues.get(0));
        final String cursorPostId = cursorValues.get(1);
        return findActiveByCursor(cursorCreatedAt, cursorPostId, PageRequest.of(0, limit));
    }

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
}
