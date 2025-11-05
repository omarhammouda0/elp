package com.example.demo.enrollment;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EnrollmentUpdateDto(

        @DecimalMin(value = "0.0", message = "Grade must be at least 0.0")

        @DecimalMax(value = "100.0", message = "Grade must be at most 100.0")
        BigDecimal finalGrade,

        Progress progress,

        Boolean isActive,

        LocalDateTime completionDate
) {}