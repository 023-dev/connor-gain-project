package com.kbt.backend.core.post.infrastructure;

import com.kbt.backend.common.domain.CursorPage;
import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.common.utils.CursorUtils;
import com.kbt.backend.core.post.application.dto.PostResponse;
import com.kbt.backend.core.user.domain.User;
import com.kbt.backend.core.user.infrastructure.UserRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.kbt.backend.core.post.domain.QPost.post;
import static com.kbt.backend.core.post.domain.QPostStat.postStat;
import static com.kbt.backend.core.post.domain.like.QLike.like;
import static com.kbt.backend.core.user.domain.QUser.user;

@Repository
@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPage<PostResponse> findAllByCursor(final Optional<String> loginUserKey, final String cursor, final int limit) {
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

        StringExpression nicknameExpression = user.nickname.coalesce("알 수 없음");

        BooleanExpression isLikedExpression;
        if (loginUserId != null) {
            isLikedExpression = like.isNotNull();
        } else {
            isLikedExpression = Expressions.asBoolean(false);
        }

        JPAQuery<PostResponse> query = queryFactory.select(
                Projections.constructor(
                        PostResponse.class,
                        post.key,
                        post.userKey,
                        nicknameExpression,
                        post.title,
                        post.content,
                        post.imageUrl,
                        postStat.likeCount,
                        postStat.commentCount,
                        postStat.viewCount,
                        isLikedExpression,
                        post.createdAt
                )
        ).from(post)
        .leftJoin(user).on(post.userId.eq(user.id))
        .join(postStat).on(post.id.eq(postStat.id));

        if (loginUserId != null) {
            query.leftJoin(like).on(
                    post.id.eq(like.postId)
                    .and(like.userId.eq(loginUserId))
                    .and(like.deletedAt.isNull())
            );
        }

        query.where(post.deletedAt.isNull());
        if (cursorCreatedAt != null && cursorPostKey != null) {
            query.where(
                    post.createdAt.before(cursorCreatedAt)
                    .or(post.createdAt.eq(cursorCreatedAt).and(post.key.lt(cursorPostKey)))
            );
        }

        query.orderBy(post.createdAt.desc(), post.key.desc());

        List<PostResponse> results = query.limit(limit + 1).fetch();

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
