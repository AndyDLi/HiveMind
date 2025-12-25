package com.andydli.hivemind.dto;

import java.time.Instant;
import java.util.List;

public record UserDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        List<PortalDTO> portals,
        ProfileDTO profile,
        Instant createdAt,
        Instant updatedAt
) {}