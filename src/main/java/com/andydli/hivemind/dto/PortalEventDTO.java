package com.andydli.hivemind.dto;

public record PortalEventDTO(
        String eventType,
        PortalDTO portalDTO
) {}