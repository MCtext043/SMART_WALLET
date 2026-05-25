package com.smartwallet.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "assistant.gigachat")
public record GigachatProperties(
        @NotBlank String url,
        @Min(100) long connectTimeoutMs,
        @Min(100) long readTimeoutMs
) {}
