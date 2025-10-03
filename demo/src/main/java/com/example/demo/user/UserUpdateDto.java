package com.example.demo.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(

        @Size(min = 10, max = 255) String username ,
        @Email String email ,
        @Size(min = 10) String password ,
        String firstName ,
        String lastName ,
        Role role ,
        Boolean isActive) {
}


