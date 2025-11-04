package com.example.demo.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserPasswordUpdateDto(


        @NotBlank(message = "Current password is required")
        String currentPassword ,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$",
                message = "New password must be at least 10 characters long " +
                        "and contain at least one uppercase letter," +
                        " one lowercase letter," +
                        " one digit," +
                        " and one special character"
        )
        @Size(min = 10, message = "New password must be at least 10 characters long")
        @NotBlank(message = "New password is required")
        String newPassword ,

        @NotBlank(message = "Password conformation is required")
        String confirmNewPassword) {

    public UserPasswordUpdateDto {

        if (!newPassword.equals ( confirmNewPassword )) {
            throw new IllegalArgumentException ( "New password and confirm new password must match" );
        }


    }


}
