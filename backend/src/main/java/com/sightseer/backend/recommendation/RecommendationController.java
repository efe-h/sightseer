package com.sightseer.backend.recommendation;

import com.sightseer.backend.exception.InvalidTokenClaimsException;
import com.sightseer.backend.recommendation.dto.RecommendationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// its a protected endpoint
@RestController
@RequestMapping("/api/recommendations")
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
