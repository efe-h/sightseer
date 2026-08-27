package com.sightseer.backend.preference;

import com.sightseer.backend.IntegrationTestBase;
import com.sightseer.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import com.sightseer.backend.repository.UserPreferenceRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PreferenceControllerIntegrationTest
        extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
        userPreferenceRepository.deleteAll();
    }

    private static final String PREFERENCES_JSON = """
            {
              "history": 1,
              "art": 2,
              "architecture": 3,
              "nature": 4,
              "science": 5,
              "food": 1,
              "entertainment": 2,
              "shopping": 3,
              "views": 4,
              "family": 5
            }
            """;

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

    @Test
    void getPreferencesWithoutJwtReturnsUnauthorized()
            throws Exception {

        mockMvc.perform(get("/api/mypreferences"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserCanSavePreferences()
            throws Exception {

        String token = registerAndGetToken();

        mockMvc.perform(put("/api/mypreferences")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(PREFERENCES_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history").value(1))
                .andExpect(jsonPath("$.art").value(2))
                .andExpect(jsonPath("$.architecture").value(3))
                .andExpect(jsonPath("$.nature").value(4))
                .andExpect(jsonPath("$.science").value(5))
                .andExpect(jsonPath("$.food").value(1))
                .andExpect(jsonPath("$.entertainment").value(2))
                .andExpect(jsonPath("$.shopping").value(3))
                .andExpect(jsonPath("$.views").value(4))
                .andExpect(jsonPath("$.family").value(5));
    }

    @Test
    void authenticatedUserCanRetrieveSavedPreferences()
            throws Exception {

        String token = registerAndGetToken();

        mockMvc.perform(put("/api/mypreferences")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(PREFERENCES_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/mypreferences")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history").value(1))
                .andExpect(jsonPath("$.art").value(2))
                .andExpect(jsonPath("$.architecture").value(3))
                .andExpect(jsonPath("$.nature").value(4))
                .andExpect(jsonPath("$.science").value(5))
                .andExpect(jsonPath("$.food").value(1))
                .andExpect(jsonPath("$.entertainment").value(2))
                .andExpect(jsonPath("$.shopping").value(3))
                .andExpect(jsonPath("$.views").value(4))
                .andExpect(jsonPath("$.family").value(5));
    }

    @Test
    void savePreferencesWithInvalidScoresReturnsBadRequest()
            throws Exception {

        String token = registerAndGetToken();

        mockMvc.perform(put("/api/mypreferences")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "history": 0,
                          "art": 2,
                          "architecture": 3,
                          "nature": 4,
                          "science": 6,
                          "food": 1,
                          "entertainment": 2,
                          "shopping": 3,
                          "views": 4,
                          "family": 5
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("The request contains invalid fields"))
                .andExpect(jsonPath("$.fieldErrors.history")
                        .value("Value must be between 1 and 5"))
                .andExpect(jsonPath("$.fieldErrors.science")
                        .value("Value must be between 1 and 5"));
    }

    @Test
    void getPreferencesReturnsNotFoundWhenNoneAreSaved()
            throws Exception {

        String token = registerAndGetToken();

        mockMvc.perform(get("/api/mypreferences")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Preferences not found"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void savingPreferencesAgainUpdatesExistingPreferences()
            throws Exception {

        String token = registerAndGetToken();

        mockMvc.perform(put("/api/mypreferences")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(PREFERENCES_JSON))
                .andExpect(status().isOk());

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history").value(5))
                .andExpect(jsonPath("$.art").value(4))
                .andExpect(jsonPath("$.family").value(1));

        mockMvc.perform(get("/api/mypreferences")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history").value(5))
                .andExpect(jsonPath("$.art").value(4))
                .andExpect(jsonPath("$.family").value(1));
    }

    @Test
    void requestWithInvalidJwtReturnsUnauthorized()
            throws Exception {

        mockMvc.perform(get("/api/mypreferences")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized());
    }
}