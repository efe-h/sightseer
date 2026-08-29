package com.sightseer.backend.recommendation.client;

import com.sightseer.backend.recommendation.dto.RecommendationRequest;
import com.sightseer.backend.recommendation.dto.RecommendationResponse;

// we can mock this and make it easier to test the RecommendationService without needing to run the FastAPI service
public interface RecommendationClient {

    RecommendationResponse getRecommendations(
            RecommendationRequest request);
}
