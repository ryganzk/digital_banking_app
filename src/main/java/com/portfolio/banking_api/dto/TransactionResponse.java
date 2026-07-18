package com.portfolio.banking_api.dto;

import com.portfolio.banking_api.domain.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
    UUID id,
    UUID accountId,
    TransactionType type,
    BigDecimal amount,
    String currency,
    UUID counterpartyAccountId,
    UUID transferGroupId,
    Instant createdAt
) {}