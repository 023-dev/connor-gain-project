package com.kbt.backend.core.auth.application.dto;

public record AuthReissueResult(
        String accessToken,
        String refreshToken
) {

    public AuthReissueResponse toResponse() {
        return new AuthReissueResponse(accessToken);
    }
}
