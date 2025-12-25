package com.andydli.hivemind.dto;

import java.time.Instant;
import java.util.Set;

public record ProfileDTO(
        Long id,
        String bio,
        Set<String> skills,
        int totalSessions,
        double rating,
        Instant createdAt,
        Instant updatedAt
) {}