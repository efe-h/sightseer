package com.sightseer.backend.recommendation.dto;

public record RecommendationRequest(
        Integer history,
        Integer art,
        Integer architecture,
        Integer nature,
        Integer science,
        Integer food,
        Integer entertainment,
        Integer shopping,
        Integer views,
        Integer family) {
}
