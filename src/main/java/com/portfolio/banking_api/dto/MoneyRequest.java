package com.portfolio.banking_api.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;

public record MoneyRequest(
    @NotNull @Positive BigDecimal amount
) {}