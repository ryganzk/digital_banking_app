package com.portfolio.banking_api.dto;

public record AuthResponse() {
    private String token;
    private String tokenType;
    private Long expiresIn;
}