package com.sightseer.backend.recommendation;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sightseer.backend.preference.PreferenceService;
import com.sightseer.backend.preference.dto.PreferenceResponse;
import com.sightseer.backend.recommendation.client.RecommendationClient;
import com.sightseer.backend.recommendation.dto.RecommendationRequest;
import com.sightseer.backend.recommendation.dto.RecommendationResponse;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceUnitTest {

    @Mock
    private PreferenceService preferenceService;

    @Mock
    private RecommendationClient recommendationClient;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(
                preferenceService,
                recommendationClient);
    }

    @Test
    void getRecommendationsConvertsPreferencesAndReturnsClientResponse() {
        Long userId = 7L;

        PreferenceResponse preferences = new PreferenceResponse(
                5, 4, 3, 2, 1,
                5, 4, 3, 2, 1);

        // mock the repsonse to be the same as the request, so we can verify that the
        // request was constructed correctly
        RecommendationResponse clientResponse = new RecommendationResponse(
                List.of(),
                List.of());

        when(preferenceService.getPreferences(userId))
                .thenReturn(preferences);

        when(recommendationClient.getRecommendations(
                org.mockito.ArgumentMatchers.any(
                        RecommendationRequest.class)))
                .thenReturn(clientResponse);

        RecommendationResponse result = recommendationService.getRecommendations(userId);

        ArgumentCaptor<RecommendationRequest> requestCaptor = ArgumentCaptor.forClass(RecommendationRequest.class);

        verify(preferenceService).getPreferences(userId);

        verify(recommendationClient)
                .getRecommendations(requestCaptor.capture());

        RecommendationRequest capturedRequest = requestCaptor.getValue();

        assertAll(
                () -> org.junit.jupiter.api.Assertions.assertEquals(
                        5, capturedRequest.history()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(
                        4, capturedRequest.art()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(
                        3, capturedRequest.architecture()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(
                        2, capturedRequest.nature()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(
                        1, capturedRequest.science()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(
                        5, capturedRequest.food()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(
                        4, capturedRequest.entertainment()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(
                        3, capturedRequest.shopping()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(
                        2, capturedRequest.views()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(
                        1, capturedRequest.family()));

        assertSame(clientResponse, result);
    }
}