package com.sightseer.backend.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AttractionRecommendationResponse(
        @JsonProperty("wikidata_id") String wikidataId,

        String name,
        String category,
        String summary,

        Double latitude,
        Double longitude,

        @JsonProperty("image_url") String imageUrl,

        List<String> themes,

        @JsonProperty("recommended_visit_time") String recommendedVisitTime,

        @JsonProperty("estimated_visit_mins") Integer estimatedVisitMins,

        Boolean indoor,

        @JsonProperty("family_friendly") Boolean familyFriendly,

        @JsonProperty("price_level") String priceLevel,

        @JsonProperty("borough_name") String boroughName,

        @JsonProperty("cluster_id") Integer clusterId,

        @JsonProperty("cluster_label") String clusterLabel,

        @JsonProperty("match_score") Double matchScore) {
}