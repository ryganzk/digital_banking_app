package com.portfolio.banking_api.dto;

public record LoginRequest(
    String email,
    String password
) {}
