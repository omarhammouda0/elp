package com.example.demo.enrollment;
import jakarta.validation.constraints.*;

public record EnrollmentCreateDto(

        @NotNull(message = "The enrollment must have a reference to a user")
        @Positive(message = "User ID must be a positive number")
        Long userId ,

        @NotNull(message = "The enrollment must have a reference to a course")
        @Positive(message = "Course ID must be a positive number")
        Long courseId


) {


}
