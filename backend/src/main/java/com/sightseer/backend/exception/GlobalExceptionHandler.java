package com.sightseer.backend.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.time.Instant;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.stream.Collectors;
import org.springframework.validation.FieldError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // build error response with consistent structure
    private ResponseEntity<ApiError> buildErrorResponse(HttpStatus status, String message,
            Map<String, String> fieldErrors) {
        ApiError apiError = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                fieldErrors);
        return ResponseEntity.status(status).body(apiError);
    }

    // DuplicateEmailException handler
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiError> handleDuplicateEmailException(DuplicateEmailException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }

    // BadCredentialsException handler
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentialsException(BadCredentialsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), Map.of());
    }

    // MethodArgumentNotValidException handler
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "The request contains invalid fields", fieldErrors);
    }

    // UserNotFoundException handler
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFoundException(UserNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    // PreferenceNotFoundException handler
    @ExceptionHandler(PreferenceNotFoundException.class)
    public ResponseEntity<ApiError> handlePreferenceNotFoundException(PreferenceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    // InvalidTokenClaimsException handler
    @ExceptionHandler(InvalidTokenClaimsException.class)
    public ResponseEntity<ApiError> handleInvalidTokenClaimsException(InvalidTokenClaimsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), Map.of());
    }

    // RecommendationServiceUnavailableException handler
    @ExceptionHandler(RecommendationServiceUnavailableException.class)
    public ResponseEntity<ApiError> handleRecommendationServiceUnavailableException(
            RecommendationServiceUnavailableException ex) {
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), Map.of());
    }

    // RecommendationServiceTimeOutException handler
    @ExceptionHandler(RecommendationServiceTimeOutException.class)
    public ResponseEntity<ApiError> handleRecommendationServiceTimeOutException(
            RecommendationServiceTimeOutException ex) {
        return buildErrorResponse(HttpStatus.GATEWAY_TIMEOUT, ex.getMessage(), Map.of());
    }

    // InvalidRecommendationResponseException handler
    @ExceptionHandler(InvalidRecommendationResponseException.class)
    public ResponseEntity<ApiError> handleInvalidRecommendationResponseException(
            InvalidRecommendationResponseException ex) {
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), Map.of());
    }
}
