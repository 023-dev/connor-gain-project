package com.kbt.backend.core.auth.presentation;

import com.kbt.backend.core.auth.application.AuthApplicationService;
import com.kbt.backend.core.auth.application.dto.AuthReissueResponse;
import com.kbt.backend.core.auth.presentation.dto.AuthReissueRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            @Valid @RequestBody final AuthReissueRequest request
    ) {
        return ResponseEntity.ok(authService.reissue(request.refreshToken()));
    }
}
