package com.kbt.backend.core.user.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordUpdateRequest(
        @NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다.")
        @Size(max = 128, message = "현재 비밀번호는 최대 128자까지 입력 가능합니다.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
        @Size(max = 128, message = "새 비밀번호는 최대 128자까지 입력 가능합니다.")
        String newPassword
) {
}
