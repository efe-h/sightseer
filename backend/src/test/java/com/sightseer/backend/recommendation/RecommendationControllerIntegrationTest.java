package com.sightseer.backend.recommendation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import com.sightseer.backend.IntegrationTestBase;
import com.sightseer.backend.exception.InvalidRecommendationResponseException;
import com.sightseer.backend.exception.RecommendationServiceTimeOutException;
import com.sightseer.backend.exception.RecommendationServiceUnavailableException;
import com.sightseer.backend.recommendation.client.RecommendationClient;
import com.sightseer.backend.recommendation.dto.AttractionRecommendationResponse;
import com.sightseer.backend.recommendation.dto.ClusterRankingResponse;
import com.sightseer.backend.recommendation.dto.RecommendationRequest;
import com.sightseer.backend.recommendation.dto.RecommendationResponse;
import com.sightseer.backend.repository.UserPreferenceRepository;
import com.sightseer.backend.repository.UserRepository;

class RecommendationControllerIntegrationTest
        extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @MockitoBean
    private RecommendationClient recommendationClient;

    @BeforeEach
    void cleanDatabase() {
        /*
         * Preferences reference users, so delete preferences first.
         */
        userPreferenceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getRecommendationsWithoutJwtReturnsUnauthorized()
            throws Exception {

        mockMvc.perform(get("/api/recommendations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getRecommendationsWithoutSavedPreferencesReturnsNotFound()
            throws Exception {

        String token = registerAndGetToken();

        mockMvc.perform(get("/api/recommendations")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Preferences not found"));
    }

    @Test
    void authenticatedUserReceivesRecommendations()
            throws Exception {

        String token = registerAndGetToken();
        savePreferences(token);

        RecommendationResponse response = successfulRecommendationResponse();

        when(recommendationClient.getRecommendations(
                any(RecommendationRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/recommendations")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.cluster_rankings[0].cluster_id").value(19))
                .andExpect(jsonPath(
                        "$.cluster_rankings[0].cluster_label").value("Southwark — Art and Culture"))
                .andExpect(jsonPath(
                        "$.cluster_rankings[0].average_match_score").value(72.39))
                .andExpect(jsonPath(
                        "$.cluster_rankings[0].rank").value(1))
                .andExpect(jsonPath(
                        "$.top_attractions[0].name").value("Fashion and Textile Museum"))
                .andExpect(jsonPath(
                        "$.top_attractions[0].match_score").value(75.0));
    }

    @Test
    void unavailableRecommendationServiceReturnsServiceUnavailable()
            throws Exception {

        String token = registerAndGetToken();
        savePreferences(token);

        when(recommendationClient.getRecommendations(
                any(RecommendationRequest.class))).thenThrow(
                        new RecommendationServiceUnavailableException());

        mockMvc.perform(get("/api/recommendations")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error")
                        .value("Service Unavailable"))
                .andExpect(jsonPath("$.message").value(
                        "The recommendation service is currently unavailable"));
    }

    @Test
    void recommendationServiceTimeoutReturnsGatewayTimeout()
            throws Exception {

        String token = registerAndGetToken();
        savePreferences(token);

        when(recommendationClient.getRecommendations(
                any(RecommendationRequest.class))).thenThrow(
                        new RecommendationServiceTimeOutException());

        mockMvc.perform(get("/api/recommendations")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.status").value(504))
                .andExpect(jsonPath("$.error")
                        .value("Gateway Timeout"))
                .andExpect(jsonPath("$.message").value(
                        "The recommendation service took too long to respond"));
    }

    @Test
    void invalidRecommendationResponseReturnsBadGateway()
            throws Exception {

        String token = registerAndGetToken();
        savePreferences(token);

        when(recommendationClient.getRecommendations(
                any(RecommendationRequest.class))).thenThrow(
                        new InvalidRecommendationResponseException());

        mockMvc.perform(get("/api/recommendations")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error")
                        .value("Bad Gateway"))
                .andExpect(jsonPath("$.message").value(
                        "The recommendation service returned an invalid response"));
    }

    private String registerAndGetToken() throws Exception {
        MvcResult result = mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "person@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        return JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.token");
    }

    private void savePreferences(String token)
            throws Exception {

        mockMvc.perform(put("/api/mypreferences")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "history": 5,
                          "art": 4,
                          "architecture": 3,
                          "nature": 2,
                          "science": 1,
                          "food": 5,
                          "entertainment": 4,
                          "shopping": 3,
                          "views": 2,
                          "family": 1
                        }
                        """))
                .andExpect(status().isOk());
    }

    private RecommendationResponse successfulRecommendationResponse() {

        ClusterRankingResponse cluster = new ClusterRankingResponse(
                19,
                "Southwark — Art and Culture",
                72.39,
                1);

        AttractionRecommendationResponse attraction = new AttractionRecommendationResponse(
                "Q5436764",
                "Fashion and Textile Museum",
                "museum",
                "A museum focused on fashion and textiles.",
                51.4986,
                -0.0811,
                "https://example.com/image.jpg",
                List.of("art", "culture"),
                "afternoon",
                90,
                true,
                true,
                "£",
                "Southwark",
                19,
                "Southwark — Art and Culture",
                75.0);

        return new RecommendationResponse(
                List.of(cluster),
                List.of(attraction));
    }
}