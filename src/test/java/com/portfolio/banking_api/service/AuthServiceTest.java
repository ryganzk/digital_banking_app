package com.portfolio.banking_api.service;

import com.portfolio.banking_api.domain.User;
import com.portfolio.banking_api.dto.AuthResponse;
import com.portfolio.banking_api.dto.LoginRequest;
import com.portfolio.banking_api.dto.RegisterRequest;
import com.portfolio.banking_api.repository.UserRepository;
import com.portfolio.banking_api.exception.ConflictException;
import com.portfolio.banking_api.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTService jwtService;

    private AuthService authService;
    private User user;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, jwtService, passwordEncoder, 3600000L);
        user = new User(
            "alex@example.com",
            "hashed-password",
            "Alex",
            "",
            "Morgan",
            "CUSTOMER"
        );
    }

    @Test
    void register_savesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest(
            "alex@example.com",
            "password123",
            "Alex",
            "",
            "Morgan"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-token");

        AuthResponse response = authService.register(request);

        assertEquals("fake-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600000L, response.expiresIn());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
            "alex@example.com",
            "password123",
            "Alex",
            "",
            "Morgan"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    @Test
    void login_returnsToken() {
        LoginRequest request = new LoginRequest("alex@example.com", "password123");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("login-token");

        AuthResponse response = authService.login(request);

        assertEquals("login-token", response.token());
        verify(userRepository).findByEmail(request.email());
    }

    @Test
    void login_throwsWhenUserNotFound() {
        LoginRequest request = new LoginRequest("benjamin@example.com", "password123");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_throwsWhenPasswordIsIncorrect() {
        LoginRequest request = new LoginRequest("alex@example.com", "wrong-password");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        verify(passwordEncoder).matches(request.password(), user.getPasswordHash());
    }
}
