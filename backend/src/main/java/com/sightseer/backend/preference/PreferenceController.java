package com.sightseer.backend.preference;

import com.sightseer.backend.preference.dto.PreferenceRequest;
import com.sightseer.backend.preference.dto.PreferenceResponse;
import com.sightseer.backend.config.OpenApiConfig;
import com.sightseer.backend.exception.InvalidTokenClaimsException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sightseer.backend.exception.ApiError;

@RestController
@RequestMapping("/api/mypreferences")
@Tag(name = "User preferences", description = "Manage the authenticated user's interest scores")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
public class PreferenceController {
    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    // Extract user ID from JWT token
    private Long extractUserIdFromJwt(Jwt jwt) {
        Number userIdNumber = jwt.getClaim("userId");
        if (userIdNumber == null) {
            throw new InvalidTokenClaimsException("JWT does not contain userId claim");
        }
        return userIdNumber.longValue();
    }

    @Operation(summary = "Get preferences", description = "Returns the authenticated user's interest profile")
    @ApiResponse(responseCode = "200", description = "Preferences returned successfully", content = @Content(schema = @Schema(implementation = PreferenceResponse.class)))
    @ApiResponse(responseCode = "404", description = "User or Preferences not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "Invalid or missing JWT", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping
    public ResponseEntity<PreferenceResponse> getPreferences(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        Long userId = extractUserIdFromJwt(jwt);
        PreferenceResponse response = preferenceService.getPreferences(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Save preferences", description = "Creates or replaces the authenticated user's interest profile")
    @ApiResponse(responseCode = "200", description = "Preferences saved successfully", content = @Content(schema = @Schema(implementation = PreferenceResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid preference scores", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Authenticated User not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "Invalid or missing JWT", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PutMapping
    public ResponseEntity<PreferenceResponse> updatePreferences(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PreferenceRequest request) {
        Long userId = extractUserIdFromJwt(jwt);
        PreferenceResponse response = preferenceService.savePreferences(userId, request);
        return ResponseEntity.ok(response);
    }
}
