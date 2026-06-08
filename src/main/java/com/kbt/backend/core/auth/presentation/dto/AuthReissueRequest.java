package com.kbt.backend.core.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthReissueRequest(
        @NotBlank
        String refreshToken
) {
}
