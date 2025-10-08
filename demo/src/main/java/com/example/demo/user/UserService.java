package com.example.demo.user;
import com.example.demo.course.CourseRepository;
import com.example.demo.exception.model.ErrorCode;
import com.example.demo.exception.types.DuplicateResourceException;
import com.example.demo.exception.types.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service

public class UserService {


    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(CourseRepository courseRepository , UserRepository userRepository , UserMapper userMapper ,
                       PasswordEncoder passwordEncoder) {

        this.courseRepository = courseRepository;
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

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getActiveUsers(Pageable pageable) {
        return userRepository.findByActiveOrderById(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getInactiveUsers(Pageable pageable) {

        return userRepository.findByActiveFalseOrderById(pageable)
                .map(userMapper::toResponse);

    }

    @Transactional (readOnly = true)
    public UserResponseDto getUserById (long id , Authentication authentication) {


        User u = userRepository.findById ( id )
                .orElseThrow (  () -> new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString () ,
                        "User with id " + id + " not found") );

         accessValidation( u ,  authentication );


        return userMapper.toResponse ( u );
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
    public UserResponseDto getUserByEmail (String requestedEmail , Authentication authentication) {
        if (requestedEmail == null || requestedEmail.isBlank ( )) {
            throw new IllegalArgumentException ( "Email can't be blank" );
        }
        String normalizeEmail = requestedEmail.trim ( ).toLowerCase ( );

        if (!isValidEmailFormat ( normalizeEmail )) {
            throw new IllegalArgumentException ( "Email format is invalid" );
        }

        User u = userRepository.findByEmailIgnoreCase ( normalizeEmail )
                .orElseThrow (  () -> new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString () ,
                        "User with email " + requestedEmail + " not found") );

        accessValidation ( u , authentication  );

        return  userMapper.toResponse ( u );
    }

    @Transactional
    public UserResponseDto updateUser(Long id , @Valid UserUpdateDto dto , Authentication authentication) {

        Objects.requireNonNull ( dto , "User can't be null" );
        User r = userRepository.findById ( id ).orElseThrow ( () ->
                new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString () , "User with id " + id + " not found") );

        accessValidation ( r ,  authentication );

        User currentUser = getCurrentUser ( authentication );

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
            String password = dto.password ( );
            r.setPassword ( passwordEncoder.encode ( password ) );
        }

        if (dto.role() != null) {

            if (!currentUser.getRole ( ).equals ( Role.ADMIN )) {
                throw new AccessDeniedException ( "Only admins can change roles" );
            }

            r.setRole(dto.role());
        }

        if (dto.isActive ()!= null) {

            if (!currentUser.getRole ( ).equals ( Role.ADMIN )) {
                throw new AccessDeniedException ( "Only admins can change active status" );
            }

            r.setActive ( dto.isActive ( ) );
        };

        userRepository.save( r );
        return userMapper.toResponse ( r );
    }

    @Transactional
    public void deleteUser( Long id , Authentication authentication ) {
        Objects.requireNonNull ( id , "User can't be null");

        User currentUser = getCurrentUser ( authentication );

        User userToDelete = userRepository.findById ( id ).orElseThrow ( () ->
                new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString ( ) , "User with id " + id + " not found" ) );

        if ( !currentUser.getRole ( ).equals ( Role.ADMIN ) ) {
            throw new AccessDeniedException ( "Only admins can delete users" );
        }

        if ( currentUser.getId ().equals ( id ) ) {
            throw new IllegalStateException ( "Admin can not delete himself" );
        }

        if ( userToDelete.getRole ( ).equals ( Role.INSTRUCTOR ) &&
             courseRepository.activeCoursesForTheInstructor ( userToDelete.getId () ))  {
            throw new IllegalStateException ( "Instructor has active courses and cannot be deleted" );

        }

        String timestamp = String.valueOf(System.currentTimeMillis());

        userToDelete.setUserName (  userToDelete.getUserName () + "_deleted_" + timestamp  );
        userToDelete.setEmail (  userToDelete.getEmail () + "_deleted_" + timestamp  );

        userToDelete.setActive (  false );
        userRepository.save ( userToDelete );

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

    private void accessValidation(User user, Authentication authentication) {

        String currentUserEmail = authentication.getName ();

        User currentUser = userRepository.findByEmailIgnoreCase ( currentUserEmail )
                .orElseThrow ( () -> new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString () ,
                        "User with email " + currentUserEmail + " not found") );

        if ( currentUser.getRole ( ).equals ( Role.STUDENT ) && !currentUser.getId ( ).equals ( user.getId ( ) ) ) {
            throw new AccessDeniedException (
                    "Students can only access their own information"
            );
        }


        boolean instructorAccessStudentInfo = courseRepository.existsByInstructorAndStudent
                ( currentUserEmail , user.getEmail () );


        if (currentUser.getRole ().equals (  Role.INSTRUCTOR ) && !instructorAccessStudentInfo &&
        !currentUser.getId ( ).equals ( user.getId ( ) ) )
        {
            throw new AccessDeniedException (
                    "Instructor can only access their own students information"
            );
        }


    }

    private User getCurrentUser(Authentication authentication) {
        String currentUserEmail = authentication.getName ( );

        return userRepository.findByEmailIgnoreCase ( currentUserEmail )
                .orElseThrow ( () -> new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString () ,
                        "User with email " + currentUserEmail + " not found") );
    }


}
