package com.example.demo.auth;

public record AuthenticationResponseDTO(

        String accessToken ,
        String tokenType ,
        String refreshToken ,
        UserResponseDTO user
) {
}
