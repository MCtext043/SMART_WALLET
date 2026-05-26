package com.smartwallet.dto;

import java.time.Instant;

public record TransactionDto(
        Integer id,
        Integer userId,
        Integer cardId,
        Double amount,
        String category,
        Double cashbackEarned,
        Instant createdAt
) {}
