package com.kbt.backend.common.response;

import java.util.List;

public record ErrorResponse(
        String message,
        List<FieldError> errors
) {
    public ErrorResponse(String message) {
        this(message, List.of());
    }

    public record FieldError(
            String field,
            String reason
    ) {}
}
