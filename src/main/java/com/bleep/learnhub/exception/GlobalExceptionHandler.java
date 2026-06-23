package com.bleep.learnhub.exception;

import com.bleep.learnhub.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 1. Handle DTO Validation Errors (@NotBlank, @Email, etc.) ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        // Extract the specific fields that failed validation and their messages
        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .message("Validation Failed")
                .error("Validation Error")
                .errorCode("VALIDATION_ERROR")
                .data(validationErrors)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // --- 2. Handle Missing Resources (404) ---
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        ApiResponse<Void> response = ApiResponse.failure(ex.getMessage(), "RESOURCE_NOT_FOUND");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // --- 3. Handle Business Rule Violations (400) ---
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        ApiResponse<Void> response = ApiResponse.failure(ex.getMessage(), "BUSINESS_RULE_VIOLATION");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // --- 4. Handle Bad Login Credentials (401) ---
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        
        ApiResponse<Void> response = ApiResponse.failure("Invalid username or password", "BAD_CREDENTIALS");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // --- 5. Handle Role/Permission Denials (403) ---
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        
        ApiResponse<Void> response = ApiResponse.failure("You do not have permission to access this resource", "ACCESS_DENIED");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // --- 6. Fallback for all other unexpected errors (500) ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllOtherExceptions(
            Exception ex, HttpServletRequest request) {
        
        // Log the actual error so the developer can fix it, but hide the stack trace from the user
        log.error("Unhandled exception caught: ", ex);

        ApiResponse<Void> response = ApiResponse.failure("An unexpected internal server error occurred", "INTERNAL_SERVER_ERROR");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}