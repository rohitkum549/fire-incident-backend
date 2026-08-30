package com.company.firemanagement.common.exception;

import org.springframework.http.HttpStatus;

public class BaseException extends RuntimeException {
    private final ErrorCode errorCode;
    private final HttpStatus status;

    public BaseException(ErrorCode errorCode, HttpStatus status, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public BaseException(ErrorCode errorCode, HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
