package com.smartwallet.dto;

import java.time.Instant;

public record RecommendationDto(Integer id, Integer userId, String message, String type, Instant createdAt) {}
