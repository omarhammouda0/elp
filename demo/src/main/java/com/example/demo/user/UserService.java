package com.example.demo.user;
import com.example.demo.exception.model.ErrorCode;
import com.example.demo.exception.types.DuplicateResourceException;
import com.example.demo.exception.types.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service

public class UserService {


    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository , UserMapper userMapper ,
                       PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public UserResponseDto createUser(@Valid UserCreationDto userCreationDto) {

        Objects.requireNonNull(userCreationDto , "User can't be null");

        String normalizeEmail = userCreationDto.email ().trim().toLowerCase();
        String normalizeUserName = userCreationDto.username ().trim ();

        if (userRepository.existsByEmail ( normalizeEmail )) {
            throw new DuplicateResourceException ( ErrorCode.EMAIL_ALREADY_EXISTS.toString () , "Email already exists");
        }

        if (userRepository.existsByUserNameIgnoreCase(normalizeUserName) ) {
            throw new DuplicateResourceException ( ErrorCode.USER_ALREADY_EXISTS.toString () , "Username already exists" );
        }

        User user = userMapper.toUser(userCreationDto);
        user.setEmail ( normalizeEmail );
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save( user );
        return userMapper.toResponse ( savedUser );

    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional (readOnly = true)
    public UserResponseDto getUserById (long id) {

        User u = userRepository.findById ( id )
                .orElseThrow (  () -> new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString () , "User with id " + id + " not found") );
        return  userMapper.toResponse ( u );
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getUsersByRole(String r , Pageable pageable) {

        if (r == null || r.isBlank ( )) {
            throw new IllegalArgumentException ( "Role can't be blank" );
        }


        Role role = isValidRole ( r );

        return userRepository.findByRole ( role , pageable )
                .map ( userMapper::toResponse );

    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail (String email) {
        if (email == null || email.isBlank ( )) {
            throw new IllegalArgumentException ( "Email can't be blank" );
        }

        String normalizeEmail = email.trim ( ).toLowerCase ( );

        if (!isValidEmailFormat ( normalizeEmail )) {
            throw new IllegalArgumentException ( "Email format is invalid" );
        }

        User u = userRepository.findByEmailIgnoreCase ( normalizeEmail )
                .orElseThrow (  () -> new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString () ,
                        "User with email " + email + " not found") );

        return  userMapper.toResponse ( u );
    }

    @Transactional
    public UserResponseDto updateUser(Long id , @Valid UserUpdateDto dto) {

        Objects.requireNonNull ( dto , "User can't be null" );
        User r = userRepository.findById ( id ).orElseThrow ( () ->
                new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString () , "User with id " + id + " not found") );

        if (dto.username ( ) != null && !dto.username ( ).isBlank ( )) {
            String userName = dto.username ( ).trim ( );
            if (userRepository.existsByUserNameAndIdNot ( userName , r.getId () )) {
                throw new DuplicateResourceException ( ErrorCode.USER_ALREADY_EXISTS.toString () , "Username already exists" );
            }
            r.setUserName ( userName );
        }

        if (dto.email ( ) != null && !dto.email ( ).isBlank ( )) {
            String email = dto.email ( ).trim ( ).toLowerCase ( );
            if (userRepository.existsByEmailAndIdNot ( email , r.getId ( ) )) {
                throw new DuplicateResourceException (ErrorCode.EMAIL_ALREADY_EXISTS.toString (), "Email already exists");
            }
            r.setEmail ( email );
        }

        if (dto.firstName ( ) != null && !dto.firstName ( ).isBlank ( )) {
            String firstName = dto.firstName ( ).trim ( );
            r.setFirstName ( firstName );
        }

        if (dto.lastName ( ) != null && !dto.lastName ( ).isBlank ( )) {
            String lastName = dto.lastName ( ).trim ( );
            r.setLastName ( lastName );
        }

        if (dto.password ( ) != null && !dto.password ( ).isBlank ( )) {
            String password = dto.password ( ).trim ( );
            r.setPassword ( passwordEncoder.encode ( password ) );
        }

        if (dto.role() != null) {
            r.setRole(dto.role());
        }

        if (dto.isActive ()!= null) {
            r.setActive ( dto.isActive ( ) );
        };

        userRepository.save( r );
        return userMapper.toResponse ( r );
    }

    @Transactional
    public UserResponseDto deleteUser( Long id ) {
        Objects.requireNonNull ( id , "User can't be null");

        User userToDelete = userRepository.findById ( id ).orElseThrow ( () ->
                new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString () , "User with id " + id + " not found") );

        userRepository.deleteById ( id );
        return userMapper.toResponse ( userToDelete );
    }


    private Role isValidRole(String role) {
        for (Role r : Role.values ( )) {
            if (r.name ( ).equalsIgnoreCase ( role.trim ( ) )) {
                return r;
            }
        }

        throw new com.example.demo.exception.types.IllegalArgumentException (
                ErrorCode.INVALID_INPUT.toString ( ) ,
                "Role must be one of the following: ADMIN, INSTRUCTOR, STUDENT"
        );

    }

    private boolean isValidEmailFormat(String email) {

        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches ( emailRegex );

    }




}
