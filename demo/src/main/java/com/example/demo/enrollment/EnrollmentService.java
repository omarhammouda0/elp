package com.example.demo.enrollment;

import com.example.demo.course.Course;
import com.example.demo.course.Status;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.exception.model.ErrorCode;
import com.example.demo.exception.types.AccessDeniedException;
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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
public class EnrollmentService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(UserRepository userRepository, CourseRepository courseRepository,
                             EnrollmentMapper enrollmentMapper, EnrollmentRepository enrollmentRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentMapper = enrollmentMapper;
        this.enrollmentRepository = enrollmentRepository;
    }

    // ========== CREATE ENROLLMENT ==========

    @Transactional
    public EnrollmentResponseDto createEnrollment(@Valid EnrollmentCreateDto dto, Authentication authentication) {
        Objects.requireNonNull(dto, "Enrollment cannot be null");
        Objects.requireNonNull(authentication, "Authentication cannot be null");

        log.info("Starting enrollment creation for studentId: {}, courseId: {}", dto.userId(), dto.courseId());


        User currentUser = getCurrentUser(authentication);
        validateEnrollmentCreationOwnership(dto, currentUser);


        if (enrollmentRepository.existsByUserIdAndCourseId(dto.userId(), dto.courseId())) {
            log.warn("Duplicate enrollment detected for userId: {}, courseId: {}", dto.userId(), dto.courseId());
            throw new DuplicateResourceException(
                    ErrorCode.USER_ALREADY_ENROLLED_IN_COURSE.toString(),
                    "User with id " + dto.userId() + " is already enrolled in course with id " + dto.courseId()
            );
        }


        User student = validateUserForEnrollment(dto);
        Course course = validateCourseForEnrollment(dto);


        if (student.getId().equals(course.getInstructor().getId())) {
            log.error("Instructor with id {} attempted to enroll in their own course with id {}",
                    student.getId(), course.getId());
            throw new AccessDeniedException("Instructors cannot enroll in their own courses");
        }


        Enrollment toSave = enrollmentMapper.toEnrollment(dto);
        toSave.setUser(student);
        toSave.setCourse(course);
        Enrollment saved = enrollmentRepository.save(toSave);

        log.info("Enrollment created with id: {} for user id: {} in course id: {} by user: {}",
                saved.getId(), student.getId(), course.getId(), currentUser.getEmail());

        return enrollmentMapper.toEnrollmentResponseDto(saved);
    }

    // ========== UPDATE ENROLLMENT ==========

    @Transactional
    public EnrollmentResponseDto updateEnrollment(Long id, @Valid EnrollmentUpdateDto dto, Authentication authentication) {
        Objects.requireNonNull(id, "Enrollment id cannot be null");
        Objects.requireNonNull(dto, "Update data cannot be null");
        Objects.requireNonNull(authentication, "Authentication cannot be null");

        log.info("Updating enrollment with id: {}", id);


        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.ENROLLMENT_NOT_FOUND.toString(),
                        "Enrollment with id " + id + " not found"
                ));

        User currentUser = getCurrentUser(authentication);
        validateEnrollmentUpdateOwnership(enrollment, currentUser);


        BigDecimal oldGrade = enrollment.getFinalGrade();
        Progress oldProgress = enrollment.getProgress();
        Boolean oldIsActive = enrollment.getIsActive();


        if (enrollment.getProgress() == Progress.COMPLETED) {
            validateUpdateForCompletedEnrollment(dto);
        }


        if (dto.finalGrade() != null) {
            updateGrade(enrollment, dto.finalGrade());
        }


        if (dto.progress() != null) {
            updateProgress(enrollment, dto.progress());
        }


        if (dto.isActive() != null) {
            enrollment.setIsActive(dto.isActive());
        }


        if (dto.completionDate() != null) {
            updateCompletionDate(enrollment, dto.completionDate());
        }


        enrollment.setUpdatedAt(LocalDateTime.now());
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        logEnrollmentUpdate(savedEnrollment, currentUser.getEmail(), oldGrade, oldProgress, oldIsActive);

        return enrollmentMapper.toEnrollmentResponseDto(savedEnrollment);
    }

    // ========== CANCEL ENROLLMENT ==========

    @Transactional
    public EnrollmentResponseDto cancelEnrollment(Long id, Authentication authentication) {
        Objects.requireNonNull(id, "Enrollment id cannot be null");
        Objects.requireNonNull(authentication, "Authentication cannot be null");

        log.info("Canceling enrollment with id: {}", id);


        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.ENROLLMENT_NOT_FOUND.toString(),
                        "Enrollment with id " + id + " not found"
                ));


        User currentUser = getCurrentUser(authentication);
        validateEnrollmentCancellationOwnership(enrollment, currentUser);


        if (enrollment.getProgress() == Progress.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed enrollment");
        }

        if (enrollment.getProgress() == Progress.CANCELLED) {
            throw new IllegalStateException("Enrollment is already cancelled");
        }


        Progress oldProgress = enrollment.getProgress();


        enrollment.setProgress(Progress.CANCELLED);
        enrollment.setIsActive(false);
        enrollment.setUpdatedAt(LocalDateTime.now());

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        log.info("Enrollment [{}] cancelled by user [{}]. Previous status: {}",
                savedEnrollment.getId(), currentUser.getEmail(), oldProgress);

        return enrollmentMapper.toEnrollmentResponseDto(savedEnrollment);
    }

    // ========== READ OPERATIONS ==========

    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDto> getAllEnrollments(Pageable pageable, Authentication authentication) {
        Objects.requireNonNull(authentication, "Authentication cannot be null");

        User currentUser = getCurrentUser(authentication);


        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can view all enrollments");
        }

        log.debug("Admin {} viewing all enrollments", currentUser.getEmail());
        return enrollmentRepository.findAll(pageable)
                .map(enrollmentMapper::toEnrollmentResponseDto);
    }

    @Transactional(readOnly = true)
    public EnrollmentResponseDto getEnrollmentById(Long id, Authentication authentication) {
        Objects.requireNonNull(id, "Enrollment id cannot be null");
        Objects.requireNonNull(authentication, "Authentication cannot be null");

        log.debug("Fetching enrollment with id: {}", id);


        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.ENROLLMENT_NOT_FOUND.toString(),
                        "Enrollment with id " + id + " not found"
                ));


        User currentUser = getCurrentUser(authentication);
        validateEnrollmentViewOwnership(enrollment, currentUser);

        return enrollmentMapper.toEnrollmentResponseDto(enrollment);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDto> getEnrollmentsByStudentId(Long userId, Pageable pageable,
                                                                 Authentication authentication) {
        Objects.requireNonNull(userId, "User id cannot be null");
        Objects.requireNonNull(authentication, "Authentication cannot be null");

        log.debug("Fetching enrollments for userId: {}", userId);


        User targetStudent = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.USER_NOT_FOUND.toString(),
                        "User with id " + userId + " not found"
                ));


        if (!Role.STUDENT.equals(targetStudent.getRole())) {
            log.error("User with id: {} is not a student", userId);
            throw new InvalidRoleException(
                    ErrorCode.USER_IS_NOT_A_STUDENT.toString(),
                    "User with id " + userId + " is not a student"
            );
        }


        User currentUser = getCurrentUser(authentication);
        validateStudentEnrollmentsViewAccess(targetStudent, currentUser);

        return enrollmentRepository.findByUser_Id(userId, pageable)
                .map(enrollmentMapper::toEnrollmentResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDto> getEnrollmentsByInstructorId(Long instructorId, Pageable pageable,
                                                                    Authentication authentication) {
        Objects.requireNonNull(instructorId, "Instructor id cannot be null");
        Objects.requireNonNull(authentication, "Authentication cannot be null");

        log.debug("Fetching enrollments for instructorId: {}", instructorId);


        User targetInstructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.USER_NOT_FOUND.toString(),
                        "Instructor with id " + instructorId + " not found"
                ));


        if (!Role.INSTRUCTOR.equals(targetInstructor.getRole())) {
            log.error("User with id: {} is not an instructor", instructorId);
            throw new InvalidRoleException(
                    ErrorCode.USER_IS_NOT_AN_INSTRUCTOR.toString(),
                    "User with id " + instructorId + " is not an instructor"
            );
        }


        User currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(instructorId)) {
            throw new AccessDeniedException("Can only view your own instructor enrollments");
        }

        return enrollmentRepository.findByInstructorId(instructorId, pageable)
                .map(enrollmentMapper::toEnrollmentResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDto> getEnrollmentsByCourseId(Long courseId, Pageable pageable,
                                                                Authentication authentication) {
        Objects.requireNonNull(courseId, "Course id cannot be null");
        Objects.requireNonNull(authentication, "Authentication cannot be null");

        log.debug("Fetching enrollments for courseId: {}", courseId);


        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.COURSE_NOT_FOUND.toString(),
                        "Course with id " + courseId + " not found"
                ));


        User currentUser = getCurrentUser(authentication);
        validateCourseEnrollmentsViewAccess(course, currentUser);

        return enrollmentRepository.findByCourseId(courseId, pageable)
                .map(enrollmentMapper::toEnrollmentResponseDto);
    }




    private void validateEnrollmentCreationOwnership(EnrollmentCreateDto dto, User currentUser) {

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }


        if (currentUser.getRole() == Role.STUDENT) {
            if (!currentUser.getId().equals(dto.userId())) {
                throw new AccessDeniedException("Students can only enroll themselves");
            }
            return;
        }


        if (currentUser.getRole() == Role.INSTRUCTOR) {
            Course course = courseRepository.findById(dto.courseId())
                    .orElseThrow(() -> new NotFoundException(
                            ErrorCode.COURSE_NOT_FOUND.toString(),
                            "Course with id " + dto.courseId() + " not found"
                    ));

            if (!course.getInstructor().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Instructors can only enroll students in their own courses");
            }
            return;
        }

        throw new AccessDeniedException("Not authorized to create enrollments");
    }


    private void validateEnrollmentUpdateOwnership(Enrollment enrollment, User currentUser) {

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }


        if (currentUser.getRole() == Role.INSTRUCTOR) {
            if (enrollment.getCourse().getInstructor().getId().equals(currentUser.getId())) {
                return;
            }
            throw new AccessDeniedException("Instructors can only update enrollments in their own courses");
        }


        throw new AccessDeniedException("Not authorized to update enrollments");
    }


    private void validateEnrollmentCancellationOwnership(Enrollment enrollment, User currentUser) {

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }


        if (currentUser.getRole() == Role.STUDENT) {
            if (enrollment.getUser().getId().equals(currentUser.getId())) {
                return;
            }
            throw new AccessDeniedException("Students can only cancel their own enrollments");
        }


        throw new AccessDeniedException("Not authorized to cancel this enrollment");
    }


    private void validateEnrollmentViewOwnership(Enrollment enrollment, User currentUser) {

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }


        if (currentUser.getRole() == Role.STUDENT) {
            if (enrollment.getUser().getId().equals(currentUser.getId())) {
                return;
            }
            throw new AccessDeniedException("Students can only view their own enrollments");
        }


        if (currentUser.getRole() == Role.INSTRUCTOR) {
            if (enrollment.getCourse().getInstructor().getId().equals(currentUser.getId())) {
                return;
            }
            throw new AccessDeniedException("Instructors can only view enrollments in their own courses");
        }

        throw new AccessDeniedException("Not authorized to view this enrollment");
    }


    private void validateStudentEnrollmentsViewAccess(User targetStudent, User currentUser) {

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }


        if (currentUser.getRole() == Role.STUDENT) {
            if (currentUser.getId().equals(targetStudent.getId())) {
                return;
            }
            throw new AccessDeniedException("Students can only view their own enrollments");
        }


        if (currentUser.getRole() == Role.INSTRUCTOR) {
            boolean hasEnrollmentInInstructorCourse = enrollmentRepository
                    .existsByUserIdAndInstructorId(targetStudent.getId(), currentUser.getId());
            if (hasEnrollmentInInstructorCourse) {
                return;
            }
            throw new AccessDeniedException("Instructors can only view enrollments of students in their courses");
        }

        throw new AccessDeniedException("Not authorized to view these enrollments");
    }


    private void validateCourseEnrollmentsViewAccess(Course course, User currentUser) {

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }


        if (currentUser.getRole() == Role.INSTRUCTOR) {
            if (course.getInstructor().getId().equals(currentUser.getId())) {
                return;
            }
            throw new AccessDeniedException("Instructors can only view enrollments in their own courses");
        }


        throw new AccessDeniedException("Not authorized to view course enrollments");
    }





    private void validateUpdateForCompletedEnrollment(EnrollmentUpdateDto dto) {
        // Allow grade updates for completed enrollments (instructor corrections)
        if (dto.finalGrade() != null && dto.progress() == null &&
                dto.isActive() == null && dto.completionDate() == null) {
            return; // Only grade update - allowed
        }

        // Block progress changes
        if (dto.progress() != null && dto.progress() != Progress.COMPLETED) {
            throw new IllegalStateException("Cannot change progress of completed enrollment");
        }


        if (dto.isActive() != null) {
            throw new IllegalStateException("Cannot change active status of completed enrollment");
        }


        if (dto.completionDate() != null) {
            throw new IllegalStateException("Cannot change completion date of completed enrollment");
        }
    }


    private void updateGrade(Enrollment enrollment, BigDecimal grade) {
        // Validate grade range
        if (grade.compareTo(BigDecimal.ZERO) < 0 || grade.compareTo(new BigDecimal("100.0")) > 0) {
            throw new IllegalArgumentException("Grade must be between 0 and 100");
        }

        enrollment.setFinalGrade(grade);
    }


    private void updateProgress(Enrollment enrollment, Progress newProgress) {
        Progress currentProgress = enrollment.getProgress();


        if (currentProgress == newProgress) {
            return;
        }


        validateProgressTransition(currentProgress, newProgress);


        enrollment.setProgress(newProgress);


        if (newProgress == Progress.COMPLETED && enrollment.getCompletionDate() == null) {
            enrollment.setCompletionDate(LocalDateTime.now());
        }
    }


    private void validateProgressTransition(Progress current, Progress newProgress) {

        if (current == Progress.COMPLETED) {
            throw new IllegalStateException(
                    String.format("Cannot transition from COMPLETED to %s - completed enrollments are final", newProgress)
            );
        }


        if (current == Progress.CANCELLED) {
            throw new IllegalStateException(
                    String.format("Cannot transition from CANCELLED to %s - must create new enrollment", newProgress)
            );
        }


        if (newProgress == Progress.NOT_STARTED && current != Progress.NOT_STARTED) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s back to NOT_STARTED", current)
            );
        }


    }


    private void updateCompletionDate(Enrollment enrollment, LocalDateTime completionDate) {

        if (enrollment.getProgress() != Progress.COMPLETED) {
            throw new IllegalArgumentException(
                    "Cannot set completion date for incomplete enrollment. Current status: " + enrollment.getProgress()
            );
        }


        if (completionDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Completion date cannot be in the future");
        }


        if (completionDate.isBefore(enrollment.getDateOfEnrollment())) {
            throw new IllegalArgumentException("Completion date cannot be before enrollment date");
        }

        enrollment.setCompletionDate(completionDate);
    }


    private void logEnrollmentUpdate(Enrollment enrollment, String userEmail,
                                     BigDecimal oldGrade, Progress oldProgress, Boolean oldIsActive) {
        StringBuilder logMessage = new StringBuilder()
                .append("Enrollment [").append(enrollment.getId())
                .append("] updated by user [").append(userEmail).append("]");


        if (oldGrade != null && enrollment.getFinalGrade() != null &&
                !oldGrade.equals(enrollment.getFinalGrade())) {
            logMessage.append(" | Grade: ").append(oldGrade).append(" → ").append(enrollment.getFinalGrade());
        } else if (oldGrade == null && enrollment.getFinalGrade() != null) {
            logMessage.append(" | Grade set to ").append(enrollment.getFinalGrade());
        } else if (oldGrade != null && enrollment.getFinalGrade() == null) {
            logMessage.append(" | Grade removed");
        }


        if (oldProgress != null && enrollment.getProgress() != oldProgress) {
            logMessage.append(" | Progress: ").append(oldProgress).append(" → ").append(enrollment.getProgress());
        }


        if (oldIsActive != null && !oldIsActive.equals(enrollment.getIsActive())) {
            logMessage.append(" | Active: ").append(oldIsActive).append(" → ").append(enrollment.getIsActive());
        }

        log.info(logMessage.toString());
    }


    private User validateUserForEnrollment(EnrollmentCreateDto dto) {
        log.debug("Validating user with id: {}", dto.userId());

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", dto.userId());
                    return new NotFoundException(
                            ErrorCode.USER_NOT_FOUND.toString(),
                            "User with id " + dto.userId() + " not found"
                    );
                });


        if (!Role.STUDENT.equals(user.getRole())) {
            log.error("User with id: {} is not a student", user.getId());
            throw new InvalidRoleException(
                    ErrorCode.USER_IS_NOT_A_STUDENT.toString(),
                    "User with id " + user.getId() + " is not a student"
            );
        }


        if (!user.isActive()) {
            log.error("Cannot enroll inactive student with id: {}", user.getId());
            throw new InActiveException(
                    ErrorCode.INACTIVE_USER.toString(),
                    "Cannot enroll inactive student with id: " + user.getId()
            );
        }

        return user;
    }


    private Course validateCourseForEnrollment(EnrollmentCreateDto dto) {
        log.debug("Validating course with id: {}", dto.courseId());

        Course course = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> {
                    log.error("Course not found with id: {}", dto.courseId());
                    return new NotFoundException(
                            ErrorCode.COURSE_NOT_FOUND.toString(),
                            "Course with id " + dto.courseId() + " not found"
                    );
                });


        if (!course.getStatus().equals(Status.PUBLISHED)) {
            log.error("Cannot enroll in unpublished course with id: {}", course.getId());
            throw new InActiveException(
                    ErrorCode.INACTIVE_COURSE.toString(),
                    "Cannot enroll in unpublished course with id " + course.getId()
            );
        }


        if (!course.getInstructor().isActive()) {
            log.error("Cannot enroll in course with inactive instructor");
            throw new InActiveException(
                    ErrorCode.INACTIVE_USER.toString(),
                    "Cannot enroll in course with inactive instructor"
            );
        }

        return course;
    }


    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.USER_NOT_FOUND.toString(),
                        "User with email " + authentication.getName() + " not found"
                ));
    }
}