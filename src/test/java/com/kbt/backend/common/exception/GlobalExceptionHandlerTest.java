package com.kbt.backend.common.exception;

import com.kbt.backend.common.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void illegalArgumentExceptionDoesNotExposeRawMessage() {
        final ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse(ErrorType.INVALID_REQUEST));
    }

    @Test
    void illegalStateExceptionIsInternalServerError() {
        final ResponseEntity<ErrorResponse> response = handler.handleIllegalStateException();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse(ErrorType.INTERNAL_SERVER_ERROR));
    }
}
