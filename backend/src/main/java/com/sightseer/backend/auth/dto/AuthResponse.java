package com.sightseer.backend.auth.dto;

public record AuthResponse(
                // just a temporoary response until JWT is implemented
                Long id,
                String email,
                String token) {

}
