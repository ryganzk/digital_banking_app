package com.portfolio.banking_api.service;

import com.portfolio.banking_api.domain.User;
import com.portfolio.banking_api.dto.AuthResponse;
import com.portfolio.banking_api.dto.LoginRequest;
import com.portfolio.banking_api.dto.RegisterRequest;
import com.portfolio.banking_api.repository.UserRepository;
import com.portfolio.banking_api.exception.ConflictException;
import com.portfolio.banking_api.exception.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final long expirationMs;

    public AuthService(
        UserRepository userRepository,
        JWTService jwtService,
        PasswordEncoder passwordEncoder,
        @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.expirationMs = expirationMs;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("User already exists");
        }

        User user = new User(
            request.email(),
            passwordEncoder.encode(request.password()),
            request.firstName(),
            request.middleName(),
            request.lastName(),
            "CUSTOMER"
        );
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(
            jwtService.generateToken(user),
            "Bearer",
            expirationMs
        );
    }
}
