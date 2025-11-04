package com.example.demo.user;

public record UserResponseDto(


        Long id ,
        String username ,
        String email ,
        String firstName ,
        String lastName ,
        Role role
) {
}
