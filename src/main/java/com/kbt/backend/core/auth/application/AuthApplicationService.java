package com.kbt.backend.core.auth.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.auth.application.dto.AuthReissueResult;
import com.kbt.backend.core.auth.application.dto.AuthTokenResult;
import com.kbt.backend.core.user.application.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final AuthTokenService authTokenService;
    private final UserQueryService userQueryService;

    public AuthReissueResult reissue(final String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new ApiException(ErrorType.INVALID_TOKEN);
        }

        final String userId = authTokenService.parseRefreshToken(refreshToken);
        userQueryService.findActiveUser(userId);

        final AuthTokenResult tokenResult = authTokenService.reissueRefreshToken(refreshToken, userId);
        return new AuthReissueResult(tokenResult.accessToken(), tokenResult.refreshToken());
    }
}
