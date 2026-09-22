package com.headspace.shared.error;

import com.headspace.shared.api.FieldViolation;
import com.headspace.shared.api.HeadSpaceProblemDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HeadSpaceProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toViolation)
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", "Request validation failed.", request.getRequestURI(), violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<HeadSpaceProblemDetail> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<FieldViolation> violations = ex.getConstraintViolations().stream()
                .map(v -> new FieldViolation(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", "Request validation failed.", request.getRequestURI(), violations);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<HeadSpaceProblemDetail> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<HeadSpaceProblemDetail> handleConflict(ConflictException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "CONFLICT", "Conflict", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<HeadSpaceProblemDetail> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE", "Business rule violated", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    public ResponseEntity<HeadSpaceProblemDetail> handleAuthenticationRequired(AuthenticationRequiredException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "Authentication required", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<HeadSpaceProblemDetail> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "Authentication required", "Authentication is required to access this resource.", request.getRequestURI(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HeadSpaceProblemDetail> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", "You do not have permission to access this resource.", request.getRequestURI(), null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<HeadSpaceProblemDetail> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found", "Requested resource was not found.", request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HeadSpaceProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception for {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal server error", "An unexpected error occurred.", request.getRequestURI(), null);
    }

    private FieldViolation toViolation(FieldError fieldError) {
        return new FieldViolation(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private ResponseEntity<HeadSpaceProblemDetail> buildResponse(
            HttpStatus status,
            String code,
            String title,
            String detail,
            String instance,
            List<FieldViolation> violations
    ) {
        String correlationId = java.util.Optional.ofNullable(org.slf4j.MDC.get("correlationId")).orElse(UUID.randomUUID().toString());
        HeadSpaceProblemDetail problem = new HeadSpaceProblemDetail(
                "https://headspace.app/problems/" + code.toLowerCase(),
                title,
                status.value(),
                detail,
                instance,
                code,
                Instant.now(),
                correlationId,
                violations == null || violations.isEmpty() ? null : violations
        );
        return ResponseEntity.status(status).contentType(org.springframework.http.MediaType.valueOf("application/problem+json")).body(problem);
    }
}
