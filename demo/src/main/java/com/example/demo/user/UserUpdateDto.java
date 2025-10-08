package com.example.demo.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(

        @Size(min = 10, max = 255) String username ,

        @Email String email ,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$",
                message = "Password must be at least 10 characters long " +
                        "and contain at least one uppercase letter," +
                        " one lowercase letter," +
                        " one digit," +
                        " and one special character" )
        @Size(min = 10) String password ,

        String firstName ,
        String lastName ,
        Role role ,
        Boolean isActive) {
}


