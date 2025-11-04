package com.example.demo.auth;

import com.example.demo.user.Role;

import java.time.LocalDateTime;

public record UserResponseDTO(

        Long id ,
        String userName ,
        String email ,
        String firstName ,
        String lastName ,
        Role role ,
        boolean isActive ,
        LocalDateTime createdAt

) {
}
