package com.sightseer.backend.preference.dto;

import jakarta.validation.constraints.*;

// use Integer, not int, because a missing JSON field can then be detected as null.

public record PreferenceRequest(
        @NotNull(message = "A number is required") @Min(value = 1, message = "Value must be between 1 and 5") @Max(value = 5, message = "Value must be between 1 and 5") Integer history,
        @NotNull(message = "A number is required") @Min(value = 1, message = "Value must be between 1 and 5") @Max(value = 5, message = "Value must be between 1 and 5") Integer art,
        @NotNull(message = "A number is required") @Min(value = 1, message = "Value must be between 1 and 5") @Max(value = 5, message = "Value must be between 1 and 5") Integer architecture,
        @NotNull(message = "A number is required") @Min(value = 1, message = "Value must be between 1 and 5") @Max(value = 5, message = "Value must be between 1 and 5") Integer nature,
        @NotNull(message = "A number is required") @Min(value = 1, message = "Value must be between 1 and 5") @Max(value = 5, message = "Value must be between 1 and 5") Integer science,
        @NotNull(message = "A number is required") @Min(value = 1, message = "Value must be between 1 and 5") @Max(value = 5, message = "Value must be between 1 and 5") Integer food,
        @NotNull(message = "A number is required") @Min(value = 1, message = "Value must be between 1 and 5") @Max(value = 5, message = "Value must be between 1 and 5") Integer entertainment,
        @NotNull(message = "A number is required") @Min(value = 1, message = "Value must be between 1 and 5") @Max(value = 5, message = "Value must be between 1 and 5") Integer shopping,
        @NotNull(message = "A number is required") @Min(value = 1, message = "Value must be between 1 and 5") @Max(value = 5, message = "Value must be between 1 and 5") Integer views,
        @NotNull(message = "A number is required") @Min(value = 1, message = "Value must be between 1 and 5") @Max(value = 5, message = "Value must be between 1 and 5") Integer family) {
}