package com.sightseer.backend.recommendation.client;

import com.sightseer.backend.recommendation.dto.RecommendationRequest;
import com.sightseer.backend.recommendation.dto.RecommendationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// Controller
//     ↓ handles frontend HTTP request
// RecommendationService
//     ↓ coordinates the use case
// RecommendationClient
//     ↓ handles communication with FastAPI
// FastAPI recommendation service

/*
 * Communicates with the external FastAPI recommendation service.
 *
 * Its responsibility is to:
 * 1. Send user preferences to FastAPI.
 * 2. Receive FastAPI's JSON response.
 * 3. Convert that response into Java DTOs.
 */

// tells spring to manage this class as a bean, so it can be injected into other classes
@Component
public class FastApiRecommendationClient
                implements RecommendationClient {

        /*
         * RestClient is Spring's synchronous HTTP client.
         *
         * It is used to send HTTP requests to another service.
         * This is different from a controller, which receives requests.
         */
        private final RestClient restClient;

        public FastApiRecommendationClient(
                        RestClient.Builder restClientBuilder,
                        @Value("${services.recommendation.base-url}") String recommendationServiceBaseUrl) {
                this.restClient = restClientBuilder
                                .baseUrl(recommendationServiceBaseUrl)
                                .build();
        }

        @Override
        public RecommendationResponse getRecommendations(
                        RecommendationRequest request) {
                return restClient
                                // create a POST request
                                .post()
                                // set the URL path for the request
                                .uri("/recommendations")
                                // include the 10 user preference scores in the request body
                                .body(request)
                                // send the request and wait for a response
                                .retrieve()
                                // convert the JSON response into a RecommendationResponse DTO
                                .body(RecommendationResponse.class);
        }
}