package com.example.demo.user;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")

public class UserController {

    private final UserService userService;

    public UserController( UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok ( userService.getAllUsers (pageable) ) ;

    }


    @GetMapping("/active")
    @PreAuthorize ( "hasRole('ADMIN')" )

    public  ResponseEntity<Page<UserResponseDto>> getActiveUsers (
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok ( userService.getActiveUsers ( pageable ) );
    }


    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<Page<UserResponseDto>> getInactiveUsers(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok ( userService.getInactiveUsers ( pageable ) );

    }


    @GetMapping("/role/{role}")
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity <Page<UserResponseDto>> getUsersByRole (
            @PathVariable String role ,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok ( userService.getUsersByRole ( role , pageable ) );
    }


    @GetMapping("/{id}")
    public ResponseEntity <UserResponseDto> getUserById (@PathVariable Long id , Authentication authentication) {
        return ResponseEntity.ok ( userService.getUserById ( id , authentication ) );
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String email , Authentication authentication) {
        return ResponseEntity.ok ( userService.getUserByEmail ( email , authentication ) );
    }

    @PostMapping
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity <UserResponseDto> saveUser(@Valid @RequestBody UserCreationDto userCreationDto) {
        return ResponseEntity.status (  HttpStatus.CREATED ).body ( userService.createUser ( userCreationDto ) );}

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id ,
                                                      @Valid @RequestBody UserUpdateDto userUpdateDto,
                                                      Authentication authentication) {

        return ResponseEntity.status ( HttpStatus.OK ).body ( userService.updateUser ( id , userUpdateDto , authentication) );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity <UserResponseDto> deleteUser(@PathVariable Long id , Authentication authentication) {
        userService.deleteUser(id , authentication );
        return ResponseEntity.noContent().build();
    }

    }

