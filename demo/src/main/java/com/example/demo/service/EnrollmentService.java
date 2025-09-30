package com.example.demo.service;

import com.example.demo.entity.course.Course;
import com.example.demo.entity.course.Status;
import com.example.demo.entity.enrollment.Enrollment;
import com.example.demo.entity.enrollment.EnrollmentCreateDto;
import com.example.demo.entity.enrollment.EnrollmentResponseDto;
import com.example.demo.entity.user.Role;
import com.example.demo.entity.user.User;
import com.example.demo.exception.model.ErrorCode;
import com.example.demo.exception.types.DuplicateResourceException;
import com.example.demo.exception.types.InActiveException;
import com.example.demo.exception.types.InvalidRoleException;
import com.example.demo.exception.types.NotFoundException;
import com.example.demo.mapper.EnrollmentMapper;
import com.example.demo.repo.CourseRepository;
import com.example.demo.repo.EnrollmentRepository;
import com.example.demo.repo.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
public class EnrollmentService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(UserRepository userRepository , CourseRepository courseRepository ,
                             EnrollmentMapper enrollmentMapper , EnrollmentRepository enrollmentRepository) {

        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentMapper = enrollmentMapper;
        this.enrollmentRepository = enrollmentRepository;
    }



    // Method to create a new enrollment

    @Transactional
    public EnrollmentResponseDto createEnrollment(@Valid EnrollmentCreateDto dto) {

        Objects.requireNonNull ( dto , "Enrollment cannot be) null" );

        log.info ( "Starting enrollment creation for userId: {}, courseId: {}" , dto.userId ( ) , dto.courseId ( ) );

        if (enrollmentRepository.existsByUserIdAndCourseId ( dto.userId ( ) , dto.courseId ( ) )) {
            log.warn ( "Duplicate enrollment detected for userId: {}, courseId: {}" , dto.userId ( ) , dto.courseId ( ) );

            throw new DuplicateResourceException ( ErrorCode.USER_ALREADY_ENROLLED_IN_COURSE.toString ( ) ,
                    "User with id " + dto.userId ( ) +
                            " is already enrolled in course with id " + dto.courseId ( ) );
        }


        log.debug ( "Fetching user with id: {}" , dto.userId ( ) );
        User user = userRepository.findById ( dto.userId ( ) )
                .orElseThrow ( () -> {
                    log.error ( "User not found with id: {}" , dto.userId ( ) );
                    return new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString ( ) ,
                            "User with id " + dto.userId ( ) + " not found" );
                } );


        if (!Role.STUDENT.equals ( user.getRole ( )) ) {

            log.error ( "User with id: {} is not a student " , dto.userId ( ) );
            throw new InvalidRoleException ( ErrorCode.USER_IS_NOT_A_STUDENT.toString ( ) ,
                    "User with id " + dto.userId ( ) + " is not an student please give a valid User" );
        }

        if (!user.isActive ( )) {

            log.error ( "Cannot enroll an inactive student with id: {}" , dto.userId ( ) );
            throw new InActiveException ( ErrorCode.INACTIVE_USER.toString () ,
                    "Cannot enroll an inactive student with id: " + dto.userId ( ) );
        }

        log.debug ( "Fetching course with id: {}" , dto.courseId ( ) );
        Course course = courseRepository.findById ( dto.courseId ( ) )
                .orElseThrow ( () -> {
                    log.error ( "Course not found with id: {}" , dto.courseId ( ) );
                    return new NotFoundException (
                            ErrorCode.COURSE_NOT_FOUND.toString ( ) ,
                            "Course with id " + dto.courseId ( ) + " not found"
                    );
                } );


        if (!course.getStatus ( ).equals ( Status.PUBLISHED )) {
            log.error ( "Cannot enroll in an inactive course" );
            throw new InActiveException ( ErrorCode.INACTIVE_COURSE.toString () ,
                    "Cannot enroll in an inactive course" );
        }

        if (!course.getInstructor ( ).isActive ( )) {
            log.error ( "Cannot enroll in a course with an inactive instructor" );
            throw new InActiveException ( ErrorCode.INACTIVE_USER.toString ( ) ,
                    "Cannot enroll in a course with an inactive instructor" );
        }


        Enrollment toSave = enrollmentMapper.toEnrollment ( dto );
        toSave.setUser ( user );
        toSave.setCourse ( course );
        Enrollment saved = enrollmentRepository.save ( toSave );

        log.info ( "Enrollment created with id: {} for user id: {} in course id: {}" ,
                saved.getId ( ) , user.getId ( ) , course.getId ( ) );
        return enrollmentMapper.toEnrollmentResponseDto ( saved );
    }


    // Method to get an enrollment with a specific ID

    @Transactional(readOnly = true)
    public EnrollmentResponseDto getEnrollmentById(Long id) {
        log.debug("Fetching enrollment with id: {}", id);

        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.ENROLLMENT_NOT_FOUND.toString(),
                        "Enrollment with id " + id + " not found"
                ));

        return enrollmentMapper.toEnrollmentResponseDto(enrollment);
    }

    // Method to get all enrollments with pagination

    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDto> getAllEnrollments(Pageable pageable) {
    return enrollmentRepository.findAll (pageable)
            .map(enrollmentMapper::toEnrollmentResponseDto);
    }
}




