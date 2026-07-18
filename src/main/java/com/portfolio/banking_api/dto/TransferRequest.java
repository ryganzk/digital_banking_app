package com.portfolio.banking_api.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record TransferRequest(
    @NotNull @Positive BigDecimal amount,
    @NotBlank String toAccountNumber
) {}