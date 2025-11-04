package com.example.demo.module;
import jakarta.validation.constraints.*;

public record ModuleCreationDto(

        @NotBlank
        @Size (min = 10, max = 255 , message = "The title must be between 10 and 255 characters")
        String title ,

        @Size(max = 1000 , message = "Description can not exceed 1000 characters")
        String description ,

        @Positive(message = "Order index must be >= 1")
        Integer orderIndex ,

        Boolean isActive  ,

        @NotNull (message = "Course id is required")
        @Positive (message = "Course it must be a positive id")
        Long courseId


        ) {
}
