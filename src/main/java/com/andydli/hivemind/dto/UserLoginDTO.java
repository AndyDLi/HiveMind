package com.andydli.hivemind.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record UserLoginDTO(
        @NotBlank(message = "Email is Required")
        @Email(message = "Email Must Be Valid")
        String email,

        @NotBlank(message = "Password is Required")
        String plainPassword
) {}