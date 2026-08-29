package com.sightseer.backend.recommendation;

// RecommendationService needs to 
// load user preferences from the database,
// use the FastAPI client to send those preferences to the FastAPI service,
// and return the recommendations to the controller.

import com.sightseer.backend.preference.PreferenceService;
import com.sightseer.backend.preference.dto.PreferenceResponse;
import com.sightseer.backend.recommendation.client.RecommendationClient;
import com.sightseer.backend.recommendation.dto.RecommendationRequest;
import com.sightseer.backend.recommendation.dto.RecommendationResponse;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

    private final PreferenceService preferenceService;
    private final RecommendationClient recommendationClient;

    public RecommendationService(
            PreferenceService preferenceService,
            RecommendationClient recommendationClient) {
        this.preferenceService = preferenceService;
        this.recommendationClient = recommendationClient;
    }

    public RecommendationResponse getRecommendations(
            Long userId) {
        /*
         * Load the authenticated user's saved preferences.
         *
         * PreferenceService already handles the case where
         * preferences do not exist.
         */
        PreferenceResponse preferences = preferenceService.getPreferences(userId);

        /*
         * Convert the internal preference response into the
         * request contract expected by FastAPI.
         */
        RecommendationRequest request = new RecommendationRequest(
                preferences.history(),
                preferences.art(),
                preferences.architecture(),
                preferences.nature(),
                preferences.science(),
                preferences.food(),
                preferences.entertainment(),
                preferences.shopping(),
                preferences.views(),
                preferences.family());

        /*
         * Delegate all HTTP communication to the client.
         */
        return recommendationClient.getRecommendations(
                request);
    }
}