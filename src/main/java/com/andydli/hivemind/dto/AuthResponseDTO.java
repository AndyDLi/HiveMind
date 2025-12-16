package com.andydli.hivemind.dto;

public record AuthResponseDTO(
        String accessToken,
        String tokenType
) {}