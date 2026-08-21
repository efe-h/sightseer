package com.sightseer.backend.auth.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email,
        @NotBlank(message = "Password is required") @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters") String password) {
}