package com.sightseer.backend.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClusterRankingResponse(
        @JsonProperty("cluster_id") Integer clusterId,

        @JsonProperty("cluster_label") String clusterLabel,

        @JsonProperty("average_match_score") Double averageMatchScore,

        Integer rank) {
}
