package com.smartwallet.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "assistant.ollama")
public record OllamaProperties(
        boolean enabled,
        @NotBlank String baseUrl,
        @NotBlank String model,
        @Min(100) long connectTimeoutMs,
        @Min(1000) long readTimeoutMs
) {}
