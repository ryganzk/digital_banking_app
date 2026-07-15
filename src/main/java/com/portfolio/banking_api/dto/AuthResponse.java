package com.portfolio.banking_api.dto;

public record AuthResponse(
    String token,
    String tokenType,
    Long expiresIn
) {}
