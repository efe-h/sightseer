package com.sightseer.backend.recommendation;

import com.sightseer.backend.exception.InvalidTokenClaimsException;
import com.sightseer.backend.recommendation.dto.RecommendationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sightseer.backend.config.OpenApiConfig;
import com.sightseer.backend.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

// its a protected endpoint
@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations", description = """
        Generate personalised attraction recommendations
        for the authenticated user
        """)
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(
            RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    // Extract user ID from JWT token
    private Long extractUserIdFromJwt(Jwt jwt) {
        Number userIdNumber = jwt.getClaim("userId");
        if (userIdNumber == null) {
            throw new InvalidTokenClaimsException("JWT does not contain userId claim");
        }
        return userIdNumber.longValue();
    }

    @Operation(summary = "Get personalised recommendations", description = """
            Loads the authenticated user's saved preferences,
            sends them to the recommendation service and returns
            ranked geographical clusters with their top attractions.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recommendations generated successfully", content = @Content(schema = @Schema(implementation = RecommendationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "The authenticated user has not saved preferences", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "502", description = "The recommendation service returned an invalid response", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "503", description = "The recommendation service is unavailable", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "504", description = "The recommendation service timed out", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @AuthenticationPrincipal Jwt jwt) {
        /*
         * Retrieve the userId stored in the authenticated JWT.
         */
        Long userId = extractUserIdFromJwt(jwt);

        /*
         * The service loads this user's preferences and calls
         * the FastAPI recommendation service.
         */
        RecommendationResponse response = recommendationService.getRecommendations(
                userId);

        return ResponseEntity.ok(response);
    }
}
