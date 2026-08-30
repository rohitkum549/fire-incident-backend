package com.company.firemanagement.common.exception;

import com.company.firemanagement.common.logging.CorrelationFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.warn("Business exception occurred: {} | Status: {}", ex.getMessage(), ex.getStatus());
        return createErrorResponse(ex.getStatus(), ex.getErrorCode(), ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        log.warn("Request validation failed on path: {} | Errors count: {}", request.getRequestURI(), validationErrors.size());
        return createErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Request validation failed", request, validationErrors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException ex, HttpServletRequest request) {
        List<ErrorResponse.ValidationError> validationErrors = ex.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> {
                            String field = result.getMethodParameter().getParameterName();
                            return ErrorResponse.ValidationError.builder()
                                    .field(field)
                                    .message(error.getDefaultMessage())
                                    .build();
                        }))
                .collect(Collectors.toList());

        log.warn("Parameter validation failed on path: {} | Errors count: {}", request.getRequestURI(), validationErrors.size());
        return createErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Parameter validation failed", request, validationErrors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied on path: {}", request.getRequestURI());
        return createErrorResponse(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, "Access denied: insufficient privileges", request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed on path: {} | Reason: {}", request.getRequestURI(), ex.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Authentication failed: invalid or expired credentials", request, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed HTTP request body on path: {}", request.getRequestURI());
        return createErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Malformed HTTP request body", request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("HTTP method {} not supported on path: {}", ex.getMethod(), request.getRequestURI());
        return createErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.BAD_REQUEST, String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod()), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled system exception caught on path: " + request.getRequestURI(), ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.SYSTEM_ERROR, "An unexpected system error occurred", request, null);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(
            HttpStatus status, ErrorCode errorCode, String message, HttpServletRequest request, List<ErrorResponse.ValidationError> validationErrors) {
        
        String correlationId = MDC.get(CorrelationFilter.MDC_KEY);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .errorCode(errorCode.getCode())
                .message(message)
                .path(request.getRequestURI())
                .correlationId(correlationId)
                .errors(validationErrors)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}
