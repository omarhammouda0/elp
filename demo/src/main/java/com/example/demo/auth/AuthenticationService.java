package com.example.demo.auth;

import com.example.demo.exception.model.ErrorCode;
import com.example.demo.exception.types.DuplicateResourceException;
import com.example.demo.exception.types.InActiveException;
import com.example.demo.exception.types.NotFoundException;
import com.example.demo.security.JwtService;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    @Transactional
    public AuthenticationResponseDTO register (RegisterRequestDTO dto) {

        if (userRepository.existsByEmail (dto.email().trim().toLowerCase())) {
            throw new DuplicateResourceException ( ErrorCode.USER_ALREADY_EXISTS.toString () ,
                    "User with email " + dto.email () + " already exists");
        }

        if (userRepository.existsByUserNameIgnoreCase ( dto.userName().trim())) {
            throw new DuplicateResourceException ( ErrorCode.USERNAME_ALREADY_EXISTS.toString () ,
                    "User with username " + dto.userName () + " already exists");
        }

        User toSave = new User (  );

        toSave.setPassword ( passwordEncoder.encode(dto.password ()));
        toSave.setEmail ( dto.email().trim().toLowerCase() );
        toSave.setUserName ( dto.userName ().trim() );
        toSave.setFirstName ( dto.firstName().trim() );
        toSave.setLastName ( dto.lastName().trim() );
        toSave.setRole ( dto.role() != null ? dto.role() : com.example.demo.user.Role.STUDENT );
        toSave.setActive ( true );

        User savedUser = userRepository.save(toSave);

        UserResponseDTO response = new UserResponseDTO (
                savedUser.getId () ,
                savedUser.getUserName () ,
                savedUser.getEmail () ,
                savedUser.getFirstName () ,
                savedUser.getLastName () ,
                savedUser.getRole () ,
                savedUser.isActive () ,
                savedUser.getCreatedAt ()
        );

        String jwtToken = jwtService.generateToken ( savedUser );
        String refreshToken = jwtService.generateRefreshToken ( savedUser );


        return new AuthenticationResponseDTO (
                jwtToken ,
                "Bearer" ,
                refreshToken ,
                response
        );


    }

    @Transactional
    public  AuthenticationResponseDTO login (LoginRequestDTO dto) {
        authenticationManager.authenticate (
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken (
                        dto.email().trim().toLowerCase() ,
                        dto.password ()
                )
        );

        User user = userRepository.findByEmail ( dto.email().trim().toLowerCase() )
                .orElseThrow ( () -> new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString () ,
                        "User with email " + dto.email () + " not found") );

        if(! user.isActive ()){
            throw new InActiveException ( ErrorCode.INACTIVE_USER.toString () ,
                    "User with email " + dto.email () + " is inactive. Please contact support." );
        }

        user.updateLastLogin ();
        userRepository.save ( user );

        String jwtToken = jwtService.generateToken ( user );
        String refreshToken = jwtService.generateRefreshToken ( user );

        UserResponseDTO response = new UserResponseDTO (
                user.getId () ,
                user.getUserName () ,
                user.getEmail () ,
                user.getFirstName () ,
                user.getLastName () ,
                user.getRole () ,
                user.isActive () ,
                user.getCreatedAt ()
        );

        return new AuthenticationResponseDTO (
                jwtToken ,
                "Bearer" ,
                refreshToken ,
                response
        );
    }
}
