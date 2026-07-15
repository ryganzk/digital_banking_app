package com.portfolio.banking_api.dto;

import java.math.BigDecimal;

public record CreateAccountRequest(
    String accountNumber,
    String ownerName,
    BigDecimal initialBalance,
    String currency
) {}