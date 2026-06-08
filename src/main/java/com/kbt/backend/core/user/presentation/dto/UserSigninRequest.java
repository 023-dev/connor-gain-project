package com.kbt.backend.core.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserSigninRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {
}
