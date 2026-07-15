package com.portfolio.banking_api.dto;

public record RegisterRequest(
    String email,
    String password,
    String firstName,
    String middleName,
    String lastName
) {}
