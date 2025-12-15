package com.andydli.hivemind.dto;

import java.time.Instant;

public record UserDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        Instant createdAt,
        Instant updatedAt
) {}