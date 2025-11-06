package com.example.edu_platform.web;

import com.example.edu_platform.exception.BusinessRuleViolationException;
import com.example.edu_platform.exception.DomainNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(DomainNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req.getRequestURI(), List.of());
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessRuleViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Business Rule Violation", ex.getMessage(), req.getRequestURI(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldErrorItem> items = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldErrorItem(fe.getField(), humanize(fe)))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation Failed", "Invalid request", req.getRequestURI(), items);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", ex.getMessage(), req.getRequestURI(), List.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus st, String err, String msg, String path,
                                                List<ErrorResponse.FieldErrorItem> details) {
        return ResponseEntity.status(st).body(
                new ErrorResponse(Instant.now(), st.value(), err, msg, path, details)
        );
    }

    private String humanize(FieldError fe) {
        return fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value";
    }
}
