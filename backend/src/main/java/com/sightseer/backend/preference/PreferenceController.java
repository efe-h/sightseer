package com.sightseer.backend.preference;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import com.sightseer.backend.preference.dto.PreferenceResponse;
import com.sightseer.backend.preference.dto.PreferenceRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.sightseer.backend.exception.InvalidTokenClaimsException;

@RestController
@RequestMapping("/api/mypreferences")
public class PreferenceController {
    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    // Extract user ID from JWT token
    private Long extractUserIdFromJwt(Jwt jwt) {
        Number userIdNumber = jwt.getClaim("user_id");
        if (userIdNumber == null) {
            throw new InvalidTokenClaimsException("JWT does not contain user_id claim");
        }
        return userIdNumber.longValue();
    }

    // GET /api/mypreferences → 200 OK
    @GetMapping
    public ResponseEntity<PreferenceResponse> getPreferences(@AuthenticationPrincipal Jwt jwt) {
        Long userId = extractUserIdFromJwt(jwt);
        PreferenceResponse response = preferenceService.getPreferences(userId);
        return ResponseEntity.ok(response);
    }

    // PUT /api/mypreferences → 200 OK
    @PutMapping
    public ResponseEntity<PreferenceResponse> updatePreferences(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PreferenceRequest request) {
        Long userId = extractUserIdFromJwt(jwt);
        PreferenceResponse response = preferenceService.savePreferences(userId, request);
        return ResponseEntity.ok(response);
    }
}
