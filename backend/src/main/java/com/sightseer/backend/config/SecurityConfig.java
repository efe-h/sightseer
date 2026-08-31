package com.sightseer.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(Customizer.withDefaults())
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(
                                                                "/api/auth/register",
                                                                "/api/auth/login",
                                                                "/error",
                                                                // the swagger endpoints
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                .anyRequest()
                                                .authenticated())
                                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(
                                                Customizer.withDefaults()))
                                .build();
        }

        /**
         * Defines CORS (Cross-Origin Resource Sharing) policies for API endpoints.
         * This allows frontend applications on different origins (domains) to access the backend.
         * 
         * @param allowedOrigins comma-separated list of origins allowed to access the API
         *                       (defaults to http://localhost:5173 for development)
         * @return configured CORS source for Spring Security
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource(
                        @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
                CorsConfiguration configuration = new CorsConfiguration();

                // Parse the comma-separated origins from the config property and trim whitespace
                // Example: "http://localhost:5173, http://localhost:3000" → [localhost:5173, localhost:3000]
                configuration.setAllowedOrigins(
                                Arrays.stream(allowedOrigins.split(","))
                                                .map(String::trim)
                                                .toList());

                // Specify which HTTP methods are allowed from cross-origin requests
                // OPTIONS is required for CORS preflight requests
                configuration.setAllowedMethods(
                                Arrays.asList(
                                                "GET",      // read operations
                                                "POST",     // create operations
                                                "PUT",      // update operations
                                                "OPTIONS"   // preflight requests
                                ));

                // Specify which headers browsers are allowed to send in cross-origin requests
                // Authorization: for JWT bearer tokens
                // Content-Type: for specifying request body format (JSON)
                configuration.setAllowedHeaders(
                                Arrays.asList(
                                                "Authorization",
                                                "Content-Type"));

                // Disable credentials in CORS responses
                // Sightseer uses bearer tokens (in the Authorization header) rather than cookies,
                // so cross-origin credentials (cookies) are not needed and not allowed
                configuration.setAllowCredentials(false);

                // Create a CORS configuration source that maps policies to URL patterns
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                // Apply this CORS configuration to all API endpoints matching /api/**
                // This ensures all REST API routes respect these CORS rules
                source.registerCorsConfiguration(
                                "/api/**",
                                configuration);

                return source;
        }
}
