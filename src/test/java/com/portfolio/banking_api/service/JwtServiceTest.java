package com.portfolio.banking_api.service;

import com.portfolio.banking_api.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JWTService jwtService;
    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService("a-secret-key-at-least-32-bytes-long!!", 3600000L);
        user = new User(
            "alex@example.com",
            "hashed-password",
            "Alex",
            "",
            "Morgan",
            "CUSTOMER"
        );
        token = jwtService.generateToken(user);
    }

    @Test
    void generateToken_returnsToken() {
        assertNotNull(token);
    }

    @Test
    void extractUserId_returnsSameId() {
        assertEquals(user.getId().toString(), jwtService.extractUserId(token));
    }

    @Test
    void isTokenValid_returnsTrueForFreshToken() {
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_returnsFalseForGarbage() {
        assertFalse(jwtService.isTokenValid("not-a-real-token"));
    }
}
