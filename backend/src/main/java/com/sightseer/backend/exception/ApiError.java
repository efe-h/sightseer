package com.sightseer.backend.exception;

import java.time.Instant;
import java.util.Map;

// consistent error response structure for API errors

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        // most errors wont have field errors, but this is useful for validation errors
        Map<String, String> fieldErrors) {

}
