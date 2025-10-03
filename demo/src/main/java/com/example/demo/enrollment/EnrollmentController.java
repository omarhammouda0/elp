package com.example.demo.enrollment;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")

public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponseDto> createEnrollment(@Valid @RequestBody EnrollmentCreateDto dto) {
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( enrollmentService.createEnrollment ( dto ) );
    }

    @GetMapping
    public ResponseEntity<Page<EnrollmentResponseDto>> getAllEnrollments(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable)
    {
        return ResponseEntity.ok ( enrollmentService.getAllEnrollments ( pageable ) );
    }

    //haw haw haw

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponseDto> getEnrollmentById(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(id));
    }

    @GetMapping("/student/{userId}")
    public ResponseEntity<Page<EnrollmentResponseDto>> getEnrollmentsByStudentId(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable ,
            @PathVariable Long userId)
    {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudentId (userId , pageable));
    }

    @GetMapping("/instructor/{userId}")
    public ResponseEntity<Page<EnrollmentResponseDto>> getEnrollmentsByInstructorId(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable ,
            @PathVariable Long userId)
    {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByInstructorId (userId , pageable));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<EnrollmentResponseDto>> getEnrollmentsByCourseId(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable ,
            @PathVariable Long courseId)
    {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourseId (courseId , pageable));
    }

}
