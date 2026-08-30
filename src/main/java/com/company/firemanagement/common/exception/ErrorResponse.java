package com.company.firemanagement.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {
    private final Instant timestamp;
    private final int status;
    private final String errorCode;
    private final String message;
    private final String path;
    private final String correlationId;
    private final List<ValidationError> errors;

    @Getter
    @Builder
    public static class ValidationError {
        private final String field;
        private final String message;
    }
}
