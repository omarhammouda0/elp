package com.example.demo.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(

        @Size(min = 2, max = 50 , message = "Username must be between 2 and 50 characters")
        String username ,

        @Email
        String email ,
        String firstName ,
        String lastName ,
        Role role ,
        Boolean isActive) {
}


