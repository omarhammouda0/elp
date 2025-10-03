package com.example.demo.enrollment;

import com.example.demo.course.Course;
import com.example.demo.course.Status;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.exception.model.ErrorCode;
import com.example.demo.exception.types.DuplicateResourceException;
import com.example.demo.exception.types.InActiveException;
import com.example.demo.exception.types.InvalidRoleException;
import com.example.demo.exception.types.NotFoundException;
import com.example.demo.course.CourseRepository;
import com.example.demo.user.UserRepository;
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


    @Transactional
    public EnrollmentResponseDto createEnrollment(@Valid EnrollmentCreateDto dto) {

        Objects.requireNonNull ( dto , "Enrollment cannot be null" );

        log.info ( "Starting enrollment creation for userId: {}, courseId: {}" , dto.userId ( ) , dto.courseId ( ) );

        if (enrollmentRepository.existsByUserIdAndCourseId ( dto.userId ( ) , dto.courseId ( ) )) {
            log.warn ( "Duplicate enrollment detected for userId: {}, courseId: {}" , dto.userId ( ) , dto.courseId ( ) );

            throw new DuplicateResourceException ( ErrorCode.USER_ALREADY_ENROLLED_IN_COURSE.toString ( ) ,
                    "User with id " + dto.userId ( ) +
                            " is already enrolled in course with id " + dto.courseId ( ) );
        }

        log.debug ( "Fetching user with id: {}" , dto.userId ( ) );

        User user = validateUserForEnrollment ( dto );
        Course course = validateCourseForEnrollment ( dto );

        Enrollment toSave = enrollmentMapper.toEnrollment ( dto );

        toSave.setUser ( user );
        toSave.setCourse ( course );
        Enrollment saved = enrollmentRepository.save ( toSave );

        log.info ( "Enrollment created with id: {} for user id: {} in course id: {}" ,
                saved.getId ( ) , user.getId ( ) , course.getId ( ) );
        return enrollmentMapper.toEnrollmentResponseDto ( saved );
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDto> getAllEnrollments(Pageable pageable) {
        return enrollmentRepository.findAll ( pageable )
                .map ( enrollmentMapper::toEnrollmentResponseDto );
    }

    @Transactional(readOnly = true)
    public EnrollmentResponseDto getEnrollmentById(Long id) {
        log.debug ( "Fetching enrollment with id: {}" , id );

        Enrollment enrollment = enrollmentRepository.findById ( id )
                .orElseThrow ( () -> new NotFoundException (
                        ErrorCode.ENROLLMENT_NOT_FOUND.toString ( ) ,
                        "Enrollment with id " + id + " not found"
                ) );

        return enrollmentMapper.toEnrollmentResponseDto ( enrollment );
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDto> getEnrollmentsByStudentId(Long userId , Pageable pageable) {
        log.debug ( "Fetching enrollments for userId: {}" , userId );


        User user = userRepository.findById ( userId ).orElseThrow ( () -> {
            log.error ( "User not found with id: {}" , userId );
            return new NotFoundException (
                    ErrorCode.USER_NOT_FOUND.toString ( ) ,
                    "User with id " + userId + " not found"
            );
        } );

        if (!Role.STUDENT.equals ( user.getRole ( ) )) {
            log.error ( "User with id: {} is not a student " , userId );
            throw new InvalidRoleException ( ErrorCode.USER_IS_NOT_A_STUDENT.toString ( ) ,
                    "User with id " + userId + " is not an student please give a valid User" );
        }

        return enrollmentRepository.findByUser_Id ( userId , pageable )
                .map ( enrollmentMapper::toEnrollmentResponseDto );
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDto> getEnrollmentsByInstructorId(Long instructorId , Pageable pageable) {
        log.debug ( "Fetching enrollments for instructorId: {}" , instructorId );

        User instructor = userRepository.findById ( instructorId ).orElseThrow ( () -> {
            log.error ( "Instructor not found with id: {}" , instructorId );
            return new NotFoundException (
                    ErrorCode.USER_NOT_FOUND.toString ( ) ,
                    "Instructor with id " + instructorId + " not found"
            );
        } );

            if (!Role.INSTRUCTOR.equals ( instructor.getRole ( ) )) {
                log.error ( "User with id: {} is not an instructor " , instructorId );
                throw new InvalidRoleException ( ErrorCode.USER_IS_NOT_AN_INSTRUCTOR.toString ( ) ,
                        "User with id " + instructorId + " is not an instructor please give a valid User" );
            }

            return enrollmentRepository.findByInstructorId ( instructorId , pageable )
                    .map ( enrollmentMapper::toEnrollmentResponseDto );
        }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDto> getEnrollmentsByCourseId(Long courseId , Pageable pageable) {

        log.debug ( "Fetching enrollments for courseId: {}" , courseId );


        if (!courseRepository.existsById ( courseId )) {
            log.error ( "Course not found with id: {}" , courseId );
            throw new NotFoundException (
                    ErrorCode.COURSE_NOT_FOUND.toString ( ) ,
                    "Course with id " + courseId + " not found"
            );
        }
        return enrollmentRepository.findByCourseId ( courseId , pageable )
                .map ( enrollmentMapper::toEnrollmentResponseDto );

    }

//    @Transactional
//    public EnrollmentResponseDto withdrawEnrollment(Long id) {
//
//        Objects.requireNonNull ( id , "id is required" );
//
//        log.debug ( "Withdrawing enrollment with id: {}" , id );
//
//        Enrollment enrollment = enrollmentRepository.findById ( id )
//                .orElseThrow ( () -> new NotFoundException (
//                        ErrorCode.ENROLLMENT_NOT_FOUND.toString ( ) ,
//                        "Enrollment with id " + id + " not found"
//                ) );
//
//        if (!enrollment.getIsActive ( )) {
//            log.error ( "Enrollment with id: {} is already inactive" , id );
//            throw new InActiveException ( ErrorCode.ENROLLMENT_ALREADY_INACTIVE.toString ( ) ,
//                    "Enrollment with id " + id + " is already inactive" );
//        }
//
//        if (!enrollment.getUser ().isActive ()){
//            log.error ( "Cannot withdraw enrollment for an inactive user with id: {}" , enrollment.getUser ().getId () );
//            throw new InActiveException ( ErrorCode.INACTIVE_USER.toString ( ) ,
//                    "Cannot withdraw enrollment for an inactive user with id: " + enrollment.getUser ().getId () );
//        }
//
//        enrollment.setIsActive ( false );
//        Enrollment updated = enrollmentRepository.save ( enrollment );
//
//        log.info ( "Enrollment with id: {} has been withdrawn" , id );
//        return enrollmentMapper.toEnrollmentResponseDto ( updated );
//    }

    private  User validateUserForEnrollment(EnrollmentCreateDto dto) {

        log.debug ( "Fetching user with id: {}" , dto.userId ( ) );

        var u = userRepository.findById ( dto.userId ( ) )
                .orElseThrow ( () -> {
                    log.error ( "User not found with id: {}" , dto.userId ( ) );
                    return new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString ( ) ,
                            "User with id " + dto.userId ( ) + " not found" );
                } );


        if (!Role.STUDENT.equals ( u.getRole ( ) )) {

            log.error ( "User with id: {} is not a student " , u.getId ( ) );
            throw new InvalidRoleException ( ErrorCode.USER_IS_NOT_A_STUDENT.toString ( ) ,
                    "User with id " + u.getId ( ) + " is not an student please give a valid User" );
        }

        if (!u.isActive ( )) {

            log.error ( "Cannot enroll an inactive student with id: {}" , u.getId ( ) );
            throw new InActiveException ( ErrorCode.INACTIVE_USER.toString ( ) ,
                    "Cannot enroll an inactive student with id: " + u.getId ( ) );
        }
        return u;

    }

    private Course validateCourseForEnrollment (EnrollmentCreateDto dto) {


        Course c = courseRepository.findById ( dto.courseId ( ) )
                .orElseThrow ( () -> {
                    log.error ( "Course not found with id: {}" , dto.courseId ( ) );
                    return new NotFoundException (
                            ErrorCode.COURSE_NOT_FOUND.toString ( ) ,
                            "Course with id " + dto.courseId ( ) + " not found"
                    );
                } );

        if (!c.getStatus ( ).equals ( Status.PUBLISHED )) {
            log.error("Cannot enroll in unpublished course with id: {}", c.getId());
            throw new InActiveException ( ErrorCode.INACTIVE_COURSE.toString ( ) ,
                    "Cannot enroll in an inactive course with id"  );
        }

        if (!c.getInstructor ( ).isActive ( )) {
            log.error ( "Cannot enroll in a course with an inactive instructor" );
            throw new InActiveException ( ErrorCode.INACTIVE_USER.toString ( ) ,
                    "Cannot enroll in a course with an inactive instructor" );
        }

        return c;

    }
}






