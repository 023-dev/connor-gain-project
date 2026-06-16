package com.kbt.backend.core.auth.presentation;

import com.kbt.backend.core.auth.application.AuthApplicationService;
import com.kbt.backend.core.auth.application.dto.AuthReissueResult;
import com.kbt.backend.core.auth.application.dto.AuthReissueResponse;
import com.kbt.backend.core.auth.application.dto.AuthLoginResult;
import com.kbt.backend.core.auth.application.dto.AuthLoginResponse;
import com.kbt.backend.core.auth.presentation.dto.AuthLoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthApplicationService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponse> login(
            @Valid @RequestBody final AuthLoginRequest request
    ) {
        final AuthLoginResult result = authService.login(request.email(), request.password());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, RefreshTokenCookie.create(result.refreshToken()).toString())
                .body(result.toResponse());
    }

    @PostMapping("/logout")
    @Authenticated
    public ResponseEntity<Void> logout(
            @UserId final String userId,
            @AccessToken final String accessToken
    ) {
        authService.logout(userId, accessToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, RefreshTokenCookie.expire().toString())
                .build();
    }

    @PostMapping("/reissue")
    public ResponseEntity<AuthReissueResponse> reissue(
            @CookieValue(name = RefreshTokenCookie.NAME, required = false) final String refreshToken
    ) {
        final AuthReissueResult result = authService.reissue(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, RefreshTokenCookie.create(result.refreshToken()).toString())
                .body(result.toResponse());
    }
}
