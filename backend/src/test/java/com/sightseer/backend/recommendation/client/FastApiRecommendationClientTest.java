package com.sightseer.backend.recommendation.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sightseer.backend.exception.InvalidRecommendationResponseException;
import com.sightseer.backend.exception.RecommendationServiceTimeOutException;
import com.sightseer.backend.exception.RecommendationServiceUnavailableException;
import com.sightseer.backend.recommendation.dto.RecommendationRequest;
import com.sightseer.backend.recommendation.dto.RecommendationResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

class FastApiRecommendationClientTest {

    private HttpServer server;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    // checks that the client sends the correct request and correctly maps a
    // successful response
    @Test
    void sendsPreferencesAndMapsSuccessfulResponse()
            throws Exception {

        AtomicReference<String> capturedMethod = new AtomicReference<>();

        AtomicReference<String> capturedBody = new AtomicReference<>();

        startServer(exchange -> {
            capturedMethod.set(exchange.getRequestMethod());

            capturedBody.set(new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8));

            sendResponse(
                    exchange,
                    200,
                    """
                            {
                              "cluster_rankings": [
                                {
                                  "cluster_id": 19,
                                  "cluster_label": "Southwark — Art and Culture",
                                  "average_match_score": 72.39,
                                  "rank": 1
                                }
                              ],
                              "top_attractions": []
                            }
                            """);
        });

        FastApiRecommendationClient client = createClient(
                Duration.ofSeconds(2));

        RecommendationResponse response = client.getRecommendations(request());

        assertEquals("POST", capturedMethod.get());

        JsonNode requestJson = objectMapper.readTree(
                capturedBody.get());

        assertEquals(5, requestJson.get("history").asInt());
        assertEquals(4, requestJson.get("art").asInt());
        assertEquals(
                3,
                requestJson.get("architecture").asInt());
        assertEquals(2, requestJson.get("nature").asInt());
        assertEquals(1, requestJson.get("science").asInt());
        assertEquals(5, requestJson.get("food").asInt());
        assertEquals(
                4,
                requestJson.get("entertainment").asInt());
        assertEquals(3, requestJson.get("shopping").asInt());
        assertEquals(2, requestJson.get("views").asInt());
        assertEquals(1, requestJson.get("family").asInt());

        assertEquals(1, response.clusterRankings().size());
        assertEquals(
                19,
                response.clusterRankings().get(0).clusterId());
        assertEquals(
                "Southwark — Art and Culture",
                response.clusterRankings().get(0).clusterLabel());
        assertEquals(
                72.39,
                response.clusterRankings()
                        .get(0)
                        .averageMatchScore());
        assertEquals(
                1,
                response.clusterRankings().get(0).rank());
        assertEquals(0, response.topAttractions().size());
    }

    // checks that the client throws an exception when the FastAPI service returns a
    // 500 error
    @Test
    void unsuccessfulFastApiResponseThrowsInvalidResponse()
            throws Exception {

        startServer(exchange -> sendResponse(
                exchange,
                500,
                """
                        {
                          "detail": "Recommendation failed"
                        }
                        """));

        FastApiRecommendationClient client = createClient(
                Duration.ofSeconds(2));

        assertThrows(
                InvalidRecommendationResponseException.class,
                () -> client.getRecommendations(request()));
    }

    @Test
    void malformedJsonThrowsInvalidResponse()
            throws Exception {

        startServer(exchange -> sendResponse(
                exchange,
                200,
                "this is not valid JSON"));

        FastApiRecommendationClient client = createClient(
                Duration.ofSeconds(2));

        assertThrows(
                InvalidRecommendationResponseException.class,
                () -> client.getRecommendations(request()));
    }

    @Test
    void emptyResponseThrowsInvalidResponse()
            throws Exception {

        startServer(exchange -> sendResponse(exchange, 200, ""));

        FastApiRecommendationClient client = createClient(
                Duration.ofSeconds(2));

        assertThrows(
                InvalidRecommendationResponseException.class,
                () -> client.getRecommendations(request()));
    }

    @Test
    void unavailableServiceThrowsUnavailableException()
            throws Exception {

        startServer(exchange -> sendResponse(exchange, 200, "{}"));

        String baseUrl = baseUrl();

        /*
         * Stop the server but retain its URL, simulating a
         * configured FastAPI service that is not running.
         */
        server.stop(0);
        server = null;

        FastApiRecommendationClient client = new FastApiRecommendationClient(
                RestClient.builder(),
                baseUrl,
                Duration.ofMillis(200),
                Duration.ofMillis(200));

        assertThrows(
                RecommendationServiceUnavailableException.class,
                () -> client.getRecommendations(request()));
    }

    @Test
    void slowServiceThrowsTimeoutException()
            throws Exception {

        startServer(exchange -> {
            try {
                Thread.sleep(500);

                sendResponse(
                        exchange,
                        200,
                        """
                                {
                                  "cluster_rankings": [],
                                  "top_attractions": []
                                }
                                """);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (IOException ignored) {
                /*
                 * The client may close the connection after
                 * timing out, causing the server write to fail.
                 */
            }
        });

        FastApiRecommendationClient client = createClient(
                Duration.ofMillis(100));

        assertThrows(
                RecommendationServiceTimeOutException.class,
                () -> client.getRecommendations(request()));
    }

    private void startServer(HttpHandler handler)
            throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(0),
                0);

        server.createContext("/recommendations", handler);
        server.start();
    }

    private FastApiRecommendationClient createClient(
            Duration readTimeout) {
        return new FastApiRecommendationClient(
                RestClient.builder(),
                baseUrl(),
                Duration.ofSeconds(1),
                readTimeout);
    }

    private String baseUrl() {
        return "http://localhost:"
                + server.getAddress().getPort();
    }

    private RecommendationRequest request() {
        return new RecommendationRequest(
                5, 4, 3, 2, 1,
                5, 4, 3, 2, 1);
    }

    private void sendResponse(
            HttpExchange exchange,
            int status,
            String body) throws IOException {

        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json");

        exchange.sendResponseHeaders(
                status,
                responseBytes.length);

        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }
}