package com.sightseer.backend.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RecommendationResponse(
        @JsonProperty("cluster_rankings") List<ClusterRankingResponse> clusterRankings,

        @JsonProperty("top_attractions") List<AttractionRecommendationResponse> topAttractions) {
}