package com.sightseer.backend.recommendation.client;

import com.sightseer.backend.exception.InvalidRecommendationResponseException;
import com.sightseer.backend.exception.RecommendationServiceUnavailableException;
import com.sightseer.backend.recommendation.dto.RecommendationRequest;
import com.sightseer.backend.recommendation.dto.RecommendationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import java.net.http.HttpClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.time.Duration;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import com.sightseer.backend.exception.RecommendationServiceTimeOutException;

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
                /*
                 * Maximum time allowed to establish a connection
                 * with the FastAPI service.
                 */
                HttpClient httpClient = HttpClient.newBuilder()
                                .version(HttpClient.Version.HTTP_1_1)
                                .connectTimeout(Duration.ofSeconds(3))
                                .build();

                JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

                /*
                 * Maximum time Spring will wait for FastAPI to finish
                 * processing the recommendation request.
                 *
                 * Recommendation generation reads the dataset and performs
                 * calculations, so this is longer than the connection timeout.
                 */
                requestFactory.setReadTimeout(Duration.ofSeconds(10));

                this.restClient = restClientBuilder
                                .baseUrl(recommendationServiceBaseUrl)
                                .requestFactory(requestFactory)
                                .build();
        }

        // helper method to check if an exception has a specific cause in its chain of
        // causes
        private boolean hasCause(
                        Throwable exception,
                        Class<? extends Throwable> causeType) {
                Throwable current = exception;

                while (current != null) {
                        if (causeType.isInstance(current)) {
                                return true;
                        }

                        current = current.getCause();
                }

                return false;
        }

        @Override
        public RecommendationResponse getRecommendations(
                        RecommendationRequest request) {
                try {
                        RecommendationResponse response = restClient
                                        .post()
                                        .uri("/recommendations")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .body(request)
                                        .retrieve()
                                        .body(RecommendationResponse.class);
                        if (response == null) {
                                throw new InvalidRecommendationResponseException();
                        }
                        return response;
                } catch (RestClientResponseException ex) {
                        // FastAPI responded with an unsuccessful status,
                        // such as 422 or 500.
                        throw new InvalidRecommendationResponseException();

                } catch (ResourceAccessException ex) {
                        // Spring could not complete the network request.
                        if (hasCause(ex, HttpTimeoutException.class)
                                        || hasCause(ex, SocketTimeoutException.class)) {
                                throw new RecommendationServiceTimeOutException();
                        }

                        if (hasCause(ex, ConnectException.class)) {
                                throw new RecommendationServiceUnavailableException();
                        }

                        // Other network failures are treated as unavailable.
                        throw new RecommendationServiceUnavailableException();

                } catch (RestClientException ex) {
                        // The request completed, but Spring could not convert
                        // the response body into RecommendationResponse.
                        throw new InvalidRecommendationResponseException();
                }
        }
}
