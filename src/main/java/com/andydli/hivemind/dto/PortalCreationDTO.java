package com.andydli.hivemind.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PortalCreationDTO(
        @NotBlank(message = "Portal Topic is Required")
        @Size(min = 1, max = 100, message = "Portal Topic Must Be Between 1 and 100 Characters")
        String topic,

        @NotBlank(message = "Portal Description is Required")
        @Size(min = 1, max = 1000, message = "Portal Description Must Be Between 1 and 1000 Characters")
        String description
) {}