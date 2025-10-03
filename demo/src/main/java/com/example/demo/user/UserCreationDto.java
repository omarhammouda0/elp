package com.example.demo.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreationDto(
        @NotBlank @Size(min = 10, max = 255) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 10) String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        Role role
) {}
