package com.example.demo.user;

public record UserResponseDto(


        String username ,
        String email ,
        String firstName ,
        String lastName ,
        Role role
) {
}
