package com.portfolio.banking_api.dto;

public record RegisterRequest() {
    private String email;
    private String password;
    private String firstName;
    private String middleName;
    private String lastName;
}