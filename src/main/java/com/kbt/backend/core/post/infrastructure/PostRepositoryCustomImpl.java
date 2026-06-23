package com.kbt.backend.core.post.infrastructure;

import com.kbt.backend.common.domain.CursorPage;
import com.kbt.backend.common.utils.CursorUtils;
import com.kbt.backend.core.post.application.dto.PostResponse;
import com.kbt.backend.core.post.domain.Post;
import com.kbt.backend.core.post.domain.QPost;
import com.kbt.backend.core.post.domain.QPostStat;
import com.kbt.backend.core.post.domain.like.QLike;
import com.kbt.backend.core.user.domain.QUser;
import com.kbt.backend.core.user.domain.User;
import com.kbt.backend.core.user.infrastructure.UserRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.util.StringUtils;
import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PostRepositoryCustomImpl extends QuerydslRepositorySupport implements PostRepositoryCustom {

    private final UserRepository userRepository;

    public PostRepositoryCustomImpl(UserRepository userRepository) {
        super(Post.class);
        this.userRepository = userRepository;
    }

    @Override
    public CursorPage<PostResponse> findAllByCursor(final Optional<String> loginUserKey, final String cursor, final int limit) {
        QPost post = QPost.post;
        QUser user = QUser.user;
        QPostStat stat = QPostStat.postStat;
        QLike like = QLike.like;

        Long loginUserId = null;
        if (loginUserKey.isPresent() && StringUtils.hasText(loginUserKey.get())) {
            loginUserId = userRepository.findByKey(loginUserKey.get())
                    .map(User::idLong)
                    .orElse(null);
        }

        LocalDateTime cursorCreatedAt = null;
        String cursorPostKey = null;
        if (StringUtils.hasText(cursor)) {
            List<String> cursorValues = CursorUtils.decode(cursor);
            try {
                if (cursorValues == null || cursorValues.size() != 2) {
                    throw new IllegalArgumentException();
                }
                cursorCreatedAt = LocalDateTime.parse(cursorValues.get(0));
                cursorPostKey = cursorValues.get(1);
                if (!StringUtils.hasText(cursorPostKey)) {
                    throw new IllegalArgumentException();
                }
            } catch (Exception exception) {
                throw new ApiException(ErrorType.INVALID_REQUEST);
            }
        }

        JPQLQuery<Post> baseQuery = from(post);

        baseQuery.leftJoin(user).on(post.userId.eq(user.id))
                 .join(stat).on(post.id.eq(stat.id).and(post.userId.eq(stat.userId)));

        BooleanExpression isLikedExpression;
        if (loginUserId != null) {
            baseQuery.leftJoin(like).on(
                    post.id.eq(like.postId)
                    .and(post.userId.eq(like.id2))
                    .and(like.userId.eq(loginUserId))
                    .and(like.deleted.isFalse())
            );
            isLikedExpression = like.isNotNull();
        } else {
            isLikedExpression = Expressions.asBoolean(false);
        }

        baseQuery.where(post.deleted.isFalse());
        if (cursorCreatedAt != null && cursorPostKey != null) {
            baseQuery.where(
                    post.createdAt.before(cursorCreatedAt)
                    .or(post.createdAt.eq(cursorCreatedAt).and(post.key.lt(cursorPostKey)))
            );
        }

        baseQuery.orderBy(post.createdAt.desc(), post.key.desc());

        StringExpression nicknameExpression = user.nickname.coalesce("알 수 없음");

        JPQLQuery<PostResponse> projectionQuery = baseQuery.select(
                Projections.constructor(
                        PostResponse.class,
                        post.key,
                        post.userKey,
                        nicknameExpression,
                        post.title,
                        post.content,
                        post.imageUrl,
                        stat.likeCount,
                        stat.commentCount,
                        stat.viewCount,
                        isLikedExpression,
                        post.createdAt
                )
        );

        List<PostResponse> results = projectionQuery.limit(limit + 1).fetch();

        boolean hasNext = results.size() > limit;
        List<PostResponse> visibleResults = hasNext ? results.subList(0, limit) : results;
        String nextCursor = null;
        if (hasNext && !visibleResults.isEmpty()) {
            PostResponse lastItem = visibleResults.get(visibleResults.size() - 1);
            nextCursor = CursorUtils.encode(lastItem.createdAt().toString(), lastItem.postId());
        }

        return new CursorPage<>(visibleResults, nextCursor, hasNext);
    }
}
