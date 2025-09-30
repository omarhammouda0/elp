package com.example.demo.entity.module;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ModuleUpdateDto(


        @Size(min = 10, max = 255 , message = "The title must be between 10 and 255 characters")
        String title ,

        @Size(max = 1000 , message = "Description can not exceed 1000 characters")
        String description ,

        @Positive(message = "order_index must be >= 1")
        Integer orderIndex ,

        Boolean isActive ,

        @Positive (message = "Course it must be a positive id")
        Long courseId
) {
}
