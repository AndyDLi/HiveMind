package com.andydli.hivemind.dto;

import java.time.Instant;
import java.util.List;

public record UserPublicDTO(
        Long id,
        String firstName,
        String lastName,
        List<PortalDTO> portals,
        ProfileDTO profile,
        Instant createdAt,
        Instant updatedAt
) {}