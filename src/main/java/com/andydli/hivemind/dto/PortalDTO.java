package com.andydli.hivemind.dto;

import java.time.Instant;

public record PortalDTO(
        Long id,
        String topic,
        String description,
        UserPublicDTO creator,
        Instant createdAt,
        Instant updatedAt
) {}