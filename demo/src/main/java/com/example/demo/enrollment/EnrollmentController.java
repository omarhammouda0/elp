package com.example.demo.enrollment;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // ========== CREATE ENROLLMENT ==========

    /**
     * Creates a new enrollment.
     * - Students can enroll themselves
     * - Instructors can enroll students in their courses
     * - Admins can enroll anyone in any course
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    public ResponseEntity<EnrollmentResponseDto> createEnrollment(
            @Valid @RequestBody EnrollmentCreateDto dto,
            Authentication authentication) {

        EnrollmentResponseDto response = enrollmentService.createEnrollment(dto, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========== UPDATE ENROLLMENT ==========

    /**
     * Updates an existing enrollment (grade, progress, completion).
     * - Only instructors can update enrollments in their courses
     * - Admins can update any enrollment
     * - Students CANNOT update (they should use cancel endpoint)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<EnrollmentResponseDto> updateEnrollment(
            @PathVariable Long id,
            @Valid @RequestBody EnrollmentUpdateDto dto,
            Authentication authentication) {

        EnrollmentResponseDto response = enrollmentService.updateEnrollment(id, dto, authentication);
        return ResponseEntity.ok(response);
    }

    // ========== CANCEL ENROLLMENT ==========

    /**
     * Cancels an enrollment (soft delete).
     * - Students can cancel their own enrollments
     * - Admins can cancel any enrollment
     * - Instructors CANNOT cancel student enrollments
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<EnrollmentResponseDto> cancelEnrollment(
            @PathVariable Long id,
            Authentication authentication) {

        EnrollmentResponseDto response = enrollmentService.cancelEnrollment(id, authentication);
        return ResponseEntity.ok(response);
    }

    // ========== READ OPERATIONS ==========

    /**
     * Gets all enrollments in the system.
     * - Only admins can access this endpoint
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EnrollmentResponseDto>> getAllEnrollments(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication authentication) {

        Page<EnrollmentResponseDto> enrollments = enrollmentService.getAllEnrollments(pageable, authentication);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Gets a single enrollment by ID.
     * - Students can view their own enrollments
     * - Instructors can view enrollments in their courses
     * - Admins can view any enrollment
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EnrollmentResponseDto> getEnrollmentById(
            @PathVariable Long id,
            Authentication authentication) {

        EnrollmentResponseDto enrollment = enrollmentService.getEnrollmentById(id, authentication);
        return ResponseEntity.ok(enrollment);
    }

    /**
     * Gets all enrollments for a specific student.
     * - Students can view their own enrollments
     * - Instructors can view enrollments of students in their courses
     * - Admins can view any student's enrollments
     */
    @GetMapping("/student/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<EnrollmentResponseDto>> getEnrollmentsByStudentId(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication authentication) {

        Page<EnrollmentResponseDto> enrollments = enrollmentService.getEnrollmentsByStudentId(
                userId, pageable, authentication);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Gets all enrollments for courses taught by a specific instructor.
     * - Instructors can view their own course enrollments
     * - Admins can view any instructor's enrollments
     */
    @GetMapping("/instructor/{instructorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<Page<EnrollmentResponseDto>> getEnrollmentsByInstructorId(
            @PathVariable Long instructorId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication authentication) {

        Page<EnrollmentResponseDto> enrollments = enrollmentService.getEnrollmentsByInstructorId(
                instructorId, pageable, authentication);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Gets all enrollments for a specific course.
     * - Only instructors of the course can view
     * - Admins can view any course's enrollments
     * - Students CANNOT view (privacy)
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<Page<EnrollmentResponseDto>> getEnrollmentsByCourseId(
            @PathVariable Long courseId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication authentication) {

        Page<EnrollmentResponseDto> enrollments = enrollmentService.getEnrollmentsByCourseId(
                courseId, pageable, authentication);
        return ResponseEntity.ok(enrollments);
    }
}