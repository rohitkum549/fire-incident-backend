package com.company.firemanagement.common.exception;

public enum ErrorCode {
    SYSTEM_ERROR("SYS_001", "An unexpected system error occurred"),
    VALIDATION_ERROR("VAL_001", "Request validation failed"),
    UNAUTHORIZED("AUTH_001", "Authentication credentials are required or invalid"),
    FORBIDDEN("AUTH_002", "Access denied: insufficient permissions"),
    RESOURCE_NOT_FOUND("RES_001", "The requested resource was not found"),
    CONFLICT("RES_002", "Resource conflict or constraint violation"),
    BAD_REQUEST("REQ_001", "Malformed request parameter or payload"),
    DATABASE_ERROR("DB_001", "Database access error or constraint failure"),
    GATEWAY_ERROR("EXT_001", "Third-party service communication failure");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
