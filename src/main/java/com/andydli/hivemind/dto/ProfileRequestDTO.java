package com.andydli.hivemind.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record ProfileRequestDTO(
        @NotBlank(message = "Bio is Required")
        @Size(max = 1500, message = "Bio Cannot Exceed 1500 Characters")
        String bio,

        @NotNull(message = "Skills Set Cannot Be Null")
        Set<@NotBlank(message = "Skill Cannot Be Empty") String> skills
) {}