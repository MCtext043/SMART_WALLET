package com.smartwallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionCreateRequest(
        @NotNull @Positive Double amount,
        @NotBlank String category,
        @NotNull Integer cardId
) {}
