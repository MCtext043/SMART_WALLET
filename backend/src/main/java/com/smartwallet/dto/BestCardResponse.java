package com.smartwallet.dto;

public record BestCardResponse(
        Integer cardId,
        String bankName,
        String cardName,
        Integer cashbackPercentage,
        String category
) {}
