package com.sightseer.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sightseer.backend.auth.dto.AuthResponse;
import com.sightseer.backend.auth.dto.LoginRequest;
import com.sightseer.backend.auth.dto.RegisterRequest;
import com.sightseer.backend.entity.User;
import com.sightseer.backend.exception.DuplicateEmailException;
import com.sightseer.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    public void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerNormalizesEmailSavesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest(
                "  PERSON@EXAMPLE.COM ",
                "password123");
        User savedUser = user(7L, "person@example.com", "encoded-password");

        when(userRepository.existsByEmail("person@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("registration-token");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("person@example.com", userCaptor.getValue().getEmail());
        assertEquals("encoded-password", userCaptor.getValue().getPasswordHash());
        assertEquals(7L, response.id());
        assertEquals("person@example.com", response.email());
        assertEquals("registration-token", response.token());
        verify(jwtService).generateToken(savedUser);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                " PERSON@example.com ",
                "password123");
        when(userRepository.existsByEmail("person@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void loginNormalizesEmailAndReturnsTokenForValidCredentials() {
        LoginRequest request = new LoginRequest(
                " PERSON@EXAMPLE.COM ",
                "password123");
        User user = user(8L, "person@example.com", "encoded-password");

        when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("login-token");

        AuthResponse response = authService.login(request);

        assertEquals(8L, response.id());
        assertEquals("person@example.com", response.email());
        assertEquals("login-token", response.token());
        verify(jwtService).generateToken(user);
    }

    @Test
    void loginRejectsUnknownEmail() {
        LoginRequest request = new LoginRequest(
                "missing@example.com",
                "password123");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void loginRejectsIncorrectPassword() {
        LoginRequest request = new LoginRequest(
                "person@example.com",
                "wrong-password");
        User user = user(8L, "person@example.com", "encoded-password");

        when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(jwtService, never()).generateToken(any(User.class));
    }

    private User user(Long id, String email, String passwordHash) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        return user;
    }
}
