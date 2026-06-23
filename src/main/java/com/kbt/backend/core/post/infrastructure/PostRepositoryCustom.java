package com.kbt.backend.core.post.infrastructure;

import com.kbt.backend.common.domain.CursorPage;
import com.kbt.backend.core.post.application.dto.PostResponse;

import java.util.Optional;

public interface PostRepositoryCustom {
    CursorPage<PostResponse> findAllByCursor(final Optional<String> loginUserKey, final String cursor, final int limit);
}
