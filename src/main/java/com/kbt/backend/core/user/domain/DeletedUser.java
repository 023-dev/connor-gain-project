package com.kbt.backend.core.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "deleted_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeletedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_key", columnDefinition = "char(36)", nullable = false)
    private String userKey;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    @Builder
    public DeletedUser(
            final String userKey,
            final String email,
            final String nickname,
            final LocalDateTime deletedAt
    ) {
        this.userKey = userKey;
        this.email = email;
        this.nickname = nickname;
        this.deletedAt = deletedAt != null ? deletedAt : LocalDateTime.now();
    }
}
