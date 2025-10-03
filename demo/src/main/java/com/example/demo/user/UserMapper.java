package com.example.demo.user;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component

public class UserMapper {

    public UserResponseDto toResponse(User user) {

        Objects.requireNonNull(user , "user can not be null");
        return new UserResponseDto (

                user.getUserName ( ) ,
                user.getEmail ( ) ,
                user.getFirstName ( ) ,
                user.getLastName ( ) ,
                user.getRole()

        );

    }

    public User toUser(UserCreationDto userCreationDto) {

        Objects.requireNonNull(userCreationDto , "userCreationDto can not be null");
        User user = new User ( );

        user.setUserName ( userCreationDto.username ( ).trim () );
        user.setEmail ( userCreationDto.email ( ).trim ().toLowerCase (  ) );
        user.setPassword ( userCreationDto.password ( ) );
        user.setFirstName ( userCreationDto.firstName ( ).trim () );
        user.setLastName ( userCreationDto.lastName ( ).trim () );
        user.setRole (  userCreationDto.role ( ) != null ? userCreationDto.role () : Role.STUDENT );
        user.setActive ( true );

        return user;

    }

    public void applyUpdate(UserUpdateDto dto, User u) {

        Objects.requireNonNull(dto, "UserUpdateDto must not be null");
        Objects.requireNonNull(u, "User must not be null");

        if (dto.email () != null) u.setEmail ( dto.email ().trim ().toLowerCase (  ) );
        if (dto.firstName () != null) u.setFirstName ( dto.firstName ().trim () );
        if (dto.lastName () != null) u.setLastName ( dto.lastName ().trim () );
        if (dto.username () != null) u.setUserName ( dto.username ().trim () );
        if (dto.role() != null) u.setRole(dto.role());
        if (dto.isActive() != null) u.setActive(dto.isActive());


    }

}
