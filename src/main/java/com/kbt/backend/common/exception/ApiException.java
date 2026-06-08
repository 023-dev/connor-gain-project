package com.kbt.backend.common.exception;

public class ApiException extends RuntimeException {

    private final ErrorType errorCode;

    public ApiException(final ErrorType errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public ErrorType errorCode() {
        return errorCode;
    }
}
