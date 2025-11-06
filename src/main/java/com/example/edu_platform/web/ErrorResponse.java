package com.example.edu_platform.web;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorItem> details
) {
    public record FieldErrorItem(String field, String message) {}
}
