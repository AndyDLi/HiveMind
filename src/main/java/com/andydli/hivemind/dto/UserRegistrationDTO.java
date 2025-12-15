package com.andydli.hivemind.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserRegistrationDTO(
        @NotBlank(message = "Email is Required")
        @Email(message = "Email Must Be Valid")
        String email,

        @NotBlank(message = "Password is Required")
        @Size(min = 8, max = 64, message = "Password Must Be Between 8 and 64 Characters")
        String plainPassword,

        @NotBlank(message = "First Name is Required")
        @Size(min = 1, max = 100, message = "First Name Must Be Between 1 and 100 Characters")
        String firstName,

        @NotBlank(message = "Last Name is Required")
        @Size(min = 1, max = 100, message = "Last Name Must Be Between 1 and 100 Characters")
        String lastName
) {}