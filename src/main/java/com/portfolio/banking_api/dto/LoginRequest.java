package com.portfolio.banking_api.dto;

public record LoginRequest() {
    private String email;
    private String password;
}