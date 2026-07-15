package com.portfolio.banking_api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
    UUID id,
    String accountNumber,
    String ownerName,
    BigDecimal balance,
    String currency,
    Instant createdAt
) {}