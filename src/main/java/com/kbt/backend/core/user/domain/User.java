package com.kbt.backend.core.user.domain;

import com.kbt.backend.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.kbt.backend.common.utils.Functions.update;

@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(fluent = true)
public class User extends BaseEntity {
    private String id;
    private String email;
    private String password;
    private String nickname;
    private String profileImage;
    private boolean deleted;

    @Builder
    public User(
            final String id,
            final String email,
            final String password,
            final String nickname,
            final String profileImage,
            final boolean deleted
    ) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.deleted = deleted;
    }

    public UserEditor.UserEditorBuilder toEditor() {
        return UserEditor.builder()
                .nickname(nickname)
                .profileImage(profileImage);
    }

    public void edit(final UserEditor editor) {
        final UserEditor.UserEditorBuilder editorBuilder = toEditor();
        update(editorBuilder::nickname, editor.nickname());
        update(editorBuilder::profileImage, editor.profileImage());

        final UserEditor mergedEditor = editorBuilder.build();
        this.nickname = mergedEditor.nickname();
        this.profileImage = mergedEditor.profileImage();
    }

    public void updatePassword(final String newPassword) {
        this.password = newPassword;
    }

    public void delete() {
        this.deleted = true;
    }
}
