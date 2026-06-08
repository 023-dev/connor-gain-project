package com.kbt.backend.common.exception;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
public enum ErrorType {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "invalid_request"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "invalid_credentials"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "invalid_token"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "forbidden"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "user_not_found"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "post_not_found"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "comment_not_found"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "duplicate_email"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "duplicate_nickname"),
    ALREADY_LIKED(HttpStatus.CONFLICT, "already_liked"),
    NOT_LIKED(HttpStatus.CONFLICT, "not_liked"),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "file_too_large"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error");

    ErrorType(final HttpStatus status, final String message) {
        this.status = status;
        this.message = message;
    }

    private final HttpStatus status;
    private final String message;
}
