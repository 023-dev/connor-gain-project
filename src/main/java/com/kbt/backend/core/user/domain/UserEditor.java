package com.kbt.backend.core.user.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class UserEditor {

    private final String nickname;
    private final String profileImage;

    @Builder
    public UserEditor(
            final String nickname,
            final String profileImage
    ) {
        this.nickname = nickname;
        this.profileImage = profileImage;
    }
}
