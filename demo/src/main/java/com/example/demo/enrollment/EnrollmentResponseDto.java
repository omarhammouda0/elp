package com.example.demo.enrollment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EnrollmentResponseDto (

        Long id,
        String studentName,
        String courseTitle,
        LocalDateTime dateOfEnrollment ,
        Boolean isActive,
        Progress progress,
        LocalDateTime completionDate,
        BigDecimal finalGrade

) {
}
