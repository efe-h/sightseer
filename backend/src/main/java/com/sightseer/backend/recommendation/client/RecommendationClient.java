package com.sightseer.backend.recommendation.client;

import com.sightseer.backend.recommendation.dto.RecommendationRequest;
import com.sightseer.backend.recommendation.dto.RecommendationResponse;

public interface RecommendationClient {

    RecommendationResponse getRecommendations(
            RecommendationRequest request);
}
