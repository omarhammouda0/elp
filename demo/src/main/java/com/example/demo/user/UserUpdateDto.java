package com.example.demo.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(

        @Size(min = 10, max = 255) String username ,

        @Email
        String email ,
        String firstName ,
        String lastName ,
        Role role ,
        Boolean isActive) {
}


