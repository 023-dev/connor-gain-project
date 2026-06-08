package com.kbt.backend.core.auth.application;

import com.kbt.backend.core.auth.application.dto.AuthReissueResponse;
import com.kbt.backend.core.auth.application.dto.AuthTokenResult;
import com.kbt.backend.core.user.application.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final AuthTokenService authTokenService;
    private final UserQueryService userQueryService;

    public AuthReissueResponse reissue(final String refreshToken) {
        final String userId = authTokenService.parseRefreshToken(refreshToken);
        userQueryService.findActiveUser(userId);

        final AuthTokenResult tokenResult = authTokenService.reissueRefreshToken(refreshToken, userId);
        return new AuthReissueResponse(tokenResult.accessToken(), tokenResult.refreshToken());
    }
}
