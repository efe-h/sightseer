package com.sightseer.backend.recommendation.client;

import com.sightseer.backend.recommendation.dto.RecommendationRequest;
import com.sightseer.backend.recommendation.dto.RecommendationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;

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

        // the old constructor used HTTP/2, which the FastAPI/uvicorn server does not
        // support, so we need to use HTTP/1.1 explicitly
        // public FastApiRecommendationClient(
        // RestClient.Builder restClientBuilder,
        // @Value("${services.recommendation.base-url}") String
        // recommendationServiceBaseUrl) {
        // this.restClient = restClientBuilder
        // .baseUrl(recommendationServiceBaseUrl)
        // .requestInterceptor(
        // (httpRequest, body, execution) -> {
        // System.out.println(
        // "Outgoing content type: "
        // + httpRequest.getHeaders()
        // .getContentType());

        // System.out.println(
        // "Outgoing request body: "
        // + new String(
        // body,
        // StandardCharsets.UTF_8));

        // return execution.execute(
        // httpRequest,
        // body);
        // })
        // .build();
        // }

        public FastApiRecommendationClient(
                        RestClient.Builder restClientBuilder,
                        @Value("${services.recommendation.base-url}") String recommendationServiceBaseUrl) {
                /*
                 * Uvicorn does not support Java's clear-text HTTP/2
                 * upgrade attempt, so use HTTP/1.1 explicitly.
                 */
                HttpClient httpClient = HttpClient
                                .newBuilder()
                                .version(HttpClient.Version.HTTP_1_1)
                                .build();

                JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

                this.restClient = restClientBuilder
                                .baseUrl(recommendationServiceBaseUrl)
                                .requestFactory(requestFactory)
                                .build();
        }

        @Override
        public RecommendationResponse getRecommendations(
                        RecommendationRequest request) {
                return restClient
                                .post()
                                .uri("/recommendations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .body(request)
                                .retrieve()
                                .body(RecommendationResponse.class);
        }
}