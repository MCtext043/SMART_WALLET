package com.smartwallet.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(@NotBlank String message) {}
