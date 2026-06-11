package com.kbt.backend.common.response;

import com.kbt.backend.common.exception.ErrorType;

public record ErrorResponse(
        String code,
        String message
) {
    public ErrorResponse(ErrorType errorType) {
        this(errorType.name(), errorType.message());
    }
}
