package com.smartwallet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
        @NotBlank String phone,
        @Email @NotBlank String email,
        @NotBlank String name,
        @NotBlank @Size(max = 128) String password
) {}
