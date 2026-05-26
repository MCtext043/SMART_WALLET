package com.smartwallet.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank String phone,
        @NotBlank String password
) {}
