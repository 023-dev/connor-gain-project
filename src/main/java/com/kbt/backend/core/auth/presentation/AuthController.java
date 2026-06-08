package com.kbt.backend.core.auth.presentation;

import com.kbt.backend.core.auth.application.AuthApplicationService;
import com.kbt.backend.core.auth.application.dto.AuthReissueResult;
import com.kbt.backend.core.auth.application.dto.AuthReissueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthApplicationService authService;

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
