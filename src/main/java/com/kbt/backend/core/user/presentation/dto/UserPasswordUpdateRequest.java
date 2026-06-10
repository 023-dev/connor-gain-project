package com.kbt.backend.core.user.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record UserPasswordUpdateRequest(
        @NotBlank
        String currentPassword,

        @NotBlank
        String newPassword
) {
}
