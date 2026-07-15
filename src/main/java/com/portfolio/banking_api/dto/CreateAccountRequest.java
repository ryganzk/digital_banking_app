package com.portfolio.banking_api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateAccountRequest(
    String accountNumber,
    String ownerName,
    BigDecimal initialBalance,
    String currency
) {}