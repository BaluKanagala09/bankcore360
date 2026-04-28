package org.cts.customerservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for BankCore360.
 * Catches all exceptions and returns consistent, structured error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });

        log.warn("Validation failed for request [{}]: {}", request.getDescription(false), fieldErrors);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed. Please check field errors.")
                .fieldErrors(fieldErrors)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // ----------------------------------------------------------------
    // 2. Resource Not Found
    // ----------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {

        log.error("Resource not found: {}", ex.getMessage());

        ErrorResponse response = buildErrorResponse(
                HttpStatus.NOT_FOUND, ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ----------------------------------------------------------------
    // 3. Business Rule Violations
    // ----------------------------------------------------------------

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {

        log.error("Business exception: {}", ex.getMessage());

        ErrorResponse response = buildErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    // ----------------------------------------------------------------
    // 4. Duplicate Resource (e.g., same Aadhaar / PAN / Email)
    // ----------------------------------------------------------------

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, WebRequest request) {

        log.error("Duplicate resource: {}", ex.getMessage());

        ErrorResponse response = buildErrorResponse(
                HttpStatus.CONFLICT, ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ----------------------------------------------------------------
    // 5. KYC Not Approved
    // ----------------------------------------------------------------

    @ExceptionHandler(KycNotApprovedException.class)
    public ResponseEntity<ErrorResponse> handleKycNotApproved(
            KycNotApprovedException ex, WebRequest request) {

        log.error("KYC not approved: {}", ex.getMessage());

        ErrorResponse response = buildErrorResponse(
                HttpStatus.FORBIDDEN, ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ----------------------------------------------------------------
    // 6. Insufficient Funds
    // ----------------------------------------------------------------

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(
            InsufficientFundsException ex, WebRequest request) {

        log.error("Insufficient funds: {}", ex.getMessage());

        ErrorResponse response = buildErrorResponse(
                HttpStatus.BAD_REQUEST, ex.getMessage(), request);

        return ResponseEntity.badRequest().body(response);
    }

    // ----------------------------------------------------------------
    // 7. Security Exceptions
    // ----------------------------------------------------------------

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {

        log.error("Bad credentials attempt: {}", request.getDescription(false));

        ErrorResponse response = buildErrorResponse(
                HttpStatus.UNAUTHORIZED, "Invalid email or password.", request);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {

        log.error("Access denied for request [{}]", request.getDescription(false));

        ErrorResponse response = buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action.",
                request);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ----------------------------------------------------------------
    // 8. Catch-All Fallback
    // ----------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {

        log.error("Unexpected error at [{}]: {}", request.getDescription(false), ex.getMessage(), ex);

        ErrorResponse response = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support.",
                request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ----------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------

    private ErrorResponse buildErrorResponse(HttpStatus status, String message, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }
}
