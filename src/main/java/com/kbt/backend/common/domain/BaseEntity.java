package com.kbt.backend.common.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public abstract class BaseEntity {
    private final LocalDateTime createdAt;

    protected BaseEntity() {
        this.createdAt = LocalDateTime.now();
    }
}
