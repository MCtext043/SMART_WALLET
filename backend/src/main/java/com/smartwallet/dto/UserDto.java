package com.smartwallet.dto;

import java.time.Instant;

public record UserDto(
        int id,
        String phone,
        String email,
        String name,
        Instant createdAt
) {}
