package com.sightseer.backend.auth;

import com.sightseer.backend.auth.dto.RegisterRequest;
import com.sightseer.backend.auth.dto.LoginRequest;
import com.sightseer.backend.auth.dto.AuthResponse;
import com.sightseer.backend.entity.User;
import com.sightseer.backend.repository.UserRepository;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sightseer.backend.exception.DuplicateEmailException;
import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalisedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmail(normalisedEmail)) {
            throw new DuplicateEmailException(
                    "An account with this email already exists");
        }

        User user = new User();
        user.setEmail(normalisedEmail);
        user.setPasswordHash(
                passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                token);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalisedEmail = normalizeEmail(request.email());

        User user = userRepository.findByEmail(normalisedEmail)
                .orElseThrow(() -> new BadCredentialsException(
                        "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException(
                    "Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                token);
    }
}