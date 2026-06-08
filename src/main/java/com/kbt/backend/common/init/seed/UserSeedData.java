package com.kbt.backend.common.init.seed;

import com.kbt.backend.core.user.domain.User;

public record UserSeedData(
        String id,
        String email,
        String password,
        String nickname,
        String profileImage,
        boolean deleted
) {

    public User toUser() {
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .nickname(nickname)
                .profileImage(profileImage)
                .deleted(deleted)
                .build();
    }
}
