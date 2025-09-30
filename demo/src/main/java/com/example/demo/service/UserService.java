package com.example.demo.service;
import com.example.demo.entity.module.ModuleResponseDto;
import com.example.demo.entity.user.User;
import com.example.demo.entity.user.UserCreationDto;
import com.example.demo.entity.user.UserResponseDto;
import com.example.demo.entity.user.UserUpdateDto;
import com.example.demo.exception.model.ErrorCode;
import com.example.demo.exception.types.DuplicateResourceException;
import com.example.demo.exception.types.NotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repo.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
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

        if (userRepository.existsByUserName(normalizeUserName) ) {
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


}
