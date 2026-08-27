package com.sightseer.backend.auth;

import com.sightseer.backend.IntegrationTestBase;
import com.sightseer.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthControllerIntegrationTest extends IntegrationTestBase {

        @Autowired
        private UserRepository userRepository;

        @BeforeEach
        void cleanDatabase() {
                userRepository.deleteAll();
        }

        @Test
        void registerReturnsCreatedAndToken() throws Exception {
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "email": "PERSON@EXAMPLE.COM",
                                                  "password": "password123"
                                                }
                                                """))
                                // .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").isNumber())
                                .andExpect(jsonPath("$.email")
                                                .value("person@example.com"))
                                .andExpect(jsonPath("$.token").isNotEmpty());
        }

        @Test
        void duplicateRegistrationReturnsConflict() throws Exception {
                String requestBody = """
                                {
                                  "email": "person@example.com",
                                  "password": "password123"
                                }
                                """;

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isCreated());

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.error").value("Conflict"))
                                .andExpect(jsonPath("$.message")
                                                .value("An account with this email already exists"))
                                .andExpect(jsonPath("$.fieldErrors").isEmpty());
        }

        @Test
        void invalidRegistrationReturnsBadRequestWithFieldErrors()
                        throws Exception {

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "email": "not-an-email",
                                                  "password": "short"
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error")
                                                .value("Bad Request"))
                                .andExpect(jsonPath("$.message")
                                                .value("The request contains invalid fields"))
                                .andExpect(jsonPath("$.fieldErrors.email")
                                                .value("Email should be valid"))
                                .andExpect(jsonPath("$.fieldErrors.password")
                                                .value(
                                                                "Password must be between 8 and 30 characters"));
                assertEquals(0, userRepository.count());
        }

        @Test
        void loginReturnsOkAndTokenForValidCredentials()
                        throws Exception {

                String registrationBody = """
                                {
                                  "email": "person@example.com",
                                  "password": "password123"
                                }
                                """;

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registrationBody))
                                .andExpect(status().isCreated());

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "email": "PERSON@EXAMPLE.COM",
                                                  "password": "password123"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").isNumber())
                                .andExpect(jsonPath("$.email")
                                                .value("person@example.com"))
                                .andExpect(jsonPath("$.token").isNotEmpty());
        }

        @Test
        void loginWithIncorrectPasswordReturnsUnauthorized()
                        throws Exception {

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "email": "person@example.com",
                                                  "password": "password123"
                                                }
                                                """))
                                .andExpect(status().isCreated());

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "email": "person@example.com",
                                                  "password": "wrong-password"
                                                }
                                                """))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.error")
                                                .value("Unauthorized"))
                                .andExpect(jsonPath("$.message")
                                                .value("Invalid email or password"))
                                .andExpect(jsonPath("$.fieldErrors").isEmpty());
        }

        @Test
        void loginWithUnknownEmailReturnsUnauthorized()
                        throws Exception {

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "email": "missing@example.com",
                                                  "password": "password123"
                                                }
                                                """))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.message")
                                                .value("Invalid email or password"));
        }
}