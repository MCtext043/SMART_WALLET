package com.smartwallet.dto;

import java.time.Instant;
import java.util.Map;

public record CardDto(
        Integer id,
        Integer userId,
        String bankName,
        String cardName,
        String last4,
        Map<String, Integer> cashbackRules,
        Double limitMonthly,
        Instant createdAt
) {}
