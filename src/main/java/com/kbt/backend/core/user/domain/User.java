package com.kbt.backend.core.user.domain;
import com.kbt.backend.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.kbt.backend.common.utils.Functions.update;

@Entity
@Table(name = "users")
@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", columnDefinition = "char(36)", nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(nullable = false)
    private boolean deleted;

    @Builder
    public User(
            final String id, // 외부 UUID
            final String email,
            final String password,
            final String nickname,
            final String profileImage,
            final boolean deleted
    ) {
        this.key = id != null ? id : java.util.UUID.randomUUID().toString();
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.deleted = deleted;
    }

    public String id() {
        return this.key;
    }

    public String nickname() {
        if (this.deleted) {
            return "알 수 없음";
        }
        return this.nickname;
    }

    public Long idLong() {
        return this.id;
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
