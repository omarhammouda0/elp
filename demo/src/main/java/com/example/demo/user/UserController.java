package com.example.demo.user;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")

public class UserController {

    private final UserService userService;

    public UserController( UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok ( userService.getAllUsers (pageable) ) ;

    }

    @GetMapping("/role/{role}")
    public ResponseEntity <Page<UserResponseDto>> getUsersByRole (
            @PathVariable String role ,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok ( userService.getUsersByRole ( role , pageable ) );
    }


    @GetMapping("/{id}")
    public ResponseEntity <UserResponseDto> getUserById (@PathVariable Long id) {
        return ResponseEntity.ok ( userService.getUserById ( id ) );
    }

    @GetMapping("/email/{email}")
    public ResponseEntity <UserResponseDto> getUserByEmail (@PathVariable String email) {
        return ResponseEntity.ok ( userService.getUserByEmail ( email ) );
    }

    @PostMapping
    public ResponseEntity <UserResponseDto> saveUser(@Valid @RequestBody UserCreationDto userCreationDto) {
        return ResponseEntity.status (  HttpStatus.CREATED ).body ( userService.createUser ( userCreationDto ) );}

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id , @Valid @RequestBody UserUpdateDto userUpdateDto) {
        return ResponseEntity.status ( HttpStatus.OK ).body ( userService.updateUser ( id , userUpdateDto ) );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity <UserResponseDto> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    }

