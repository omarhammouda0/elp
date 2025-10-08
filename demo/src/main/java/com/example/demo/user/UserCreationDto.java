package com.example.demo.user;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;

public record UserCreationDto(
        @NotBlank (message = "Username cannot be blank")
        @Size(min = 2, max = 50 , message = "Username must be between 2 and 50 characters")
        String username,

        @NotBlank (message = "Email cannot be blank")
        @Email
        @Size ( min = 10 ,max = 100 , message = "Email must be between 10 and 100 characters")

        String email,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$",
                message = "Password must be at least 10 characters long " +
                        "and contain at least one uppercase letter," +
                        " one lowercase letter," +
                        " one digit," +
                        " and one special character"
        )
        String password,

        @NotBlank (message = "First name cannot be blank")
        @Size (min = 2, max = 50 , message = "First name must be between 2 and 50 characters")
        String firstName,

        @NotBlank (message = "Last name cannot be blank")
        @Size (min = 2, max = 50 , message = "Last name must be between 2 and 50 characters")
        String lastName,

        @NotNull(message = "Role cannot be blank")
        @Enumerated(EnumType.STRING)
         Role role
) {}
