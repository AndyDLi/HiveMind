package com.andydli.hivemind.dto;

import java.time.Instant;

public record PortalDTO(
        Long id,
        String topic,
        String description,
        UserDTO creator,
        Instant createdAt,
        Instant updatedAt
) {}