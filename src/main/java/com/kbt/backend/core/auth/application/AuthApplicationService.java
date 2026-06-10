package com.kbt.backend.core.auth.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.auth.application.dto.AuthReissueResult;
import com.kbt.backend.core.auth.application.dto.AuthToken;
import com.kbt.backend.core.auth.application.dto.AuthLoginResult;
import com.kbt.backend.core.user.application.UserCommandService;
import com.kbt.backend.core.user.application.UserQueryService;
import com.kbt.backend.core.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final AuthTokenService authTokenService;
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    public AuthLoginResult login(final String email, final String password) {
        final User user = userCommandService.signin(email, password);
        final AuthToken token = authTokenService.issue(user.id());
        return new AuthLoginResult(
                user.id(),
                user.nickname(),
                user.profileImage(),
                token.accessToken(),
                token.refreshToken()
        );
    }

    public void logout(final String userId, final String accessToken) {
        userCommandService.signout(userId);
        authTokenService.revokeAccessToken(accessToken);
        authTokenService.revokeRefreshTokens(userId);
    }

    public AuthReissueResult reissue(final String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new ApiException(ErrorType.INVALID_TOKEN);
        }

        final String userId = authTokenService.parseRefreshToken(refreshToken);
        userQueryService.findActiveUser(userId);

        final AuthToken tokenResult = authTokenService.reissueRefreshToken(refreshToken, userId);
        return new AuthReissueResult(tokenResult.accessToken(), tokenResult.refreshToken());
    }
}
