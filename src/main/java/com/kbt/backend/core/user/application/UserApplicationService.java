package com.kbt.backend.core.user.application;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.auth.application.AuthTokenService;
import com.kbt.backend.core.user.application.dto.UserMeResponse;
import com.kbt.backend.core.user.application.dto.UserSignupResponse;
import com.kbt.backend.core.user.application.dto.UserUpdateResponse;
import com.kbt.backend.core.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final AuthTokenService authTokenService;

    public UserSignupResponse signup(
            final String email,
            final String password,
            final String nickname,
            final String profileImage
    ) {
        final User user = userCommandService.signup(email, password, nickname, profileImage);
        return new UserSignupResponse(user.id());
    }

    public UserMeResponse me(final String userId) {
        final User user = userQueryService.me(userId);
        return new UserMeResponse(user.id(), user.email(), user.nickname(), user.profileImage());
    }

    public UserUpdateResponse update(
            final String userId,
            final String nickname,
            final String profileImage
    ) {
        validateHasUpdateValue(nickname, profileImage);

        final User user = userQueryService.findActiveUser(userId);
        final User editedUser = userCommandService.update(user, nickname, profileImage);
        return new UserUpdateResponse(editedUser.id(), editedUser.nickname(), editedUser.profileImage());
    }

    public void updatePassword(
            final String userId,
            final String currentPassword,
            final String newPassword
    ) {
        userCommandService.updatePassword(userId, currentPassword, newPassword);
    }

    public void delete(final String userId) {
        userCommandService.delete(userId);
    }

    public void delete(
            final String userId,
            final String accessToken
    ) {
        userCommandService.delete(userId);
        authTokenService.revokeAccessToken(accessToken);
        authTokenService.revokeRefreshTokens(userId);
    }

    private void validateHasUpdateValue(
            final String nickname,
            final String profileImage
    ) {
        if (!StringUtils.hasText(nickname) && !StringUtils.hasText(profileImage)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }
}
