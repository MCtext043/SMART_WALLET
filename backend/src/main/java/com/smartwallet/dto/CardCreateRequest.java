package com.smartwallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CardCreateRequest(
        @NotBlank String bankName,
        @NotBlank String cardName,
        @NotBlank String last4,
        @NotNull Map<String, Integer> cashbackRules,
        @NotNull @DecimalMin("0") Double limitMonthly
) {}
