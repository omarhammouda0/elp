package com.example.demo.course;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CourseCreateDto   (

        @NotBlank (message = "Title can not be empty")
        @Size (min = 5 , max = 255 , message = "The title must be between 5 and 255 characters")
        String title ,

        @Size(max = 1000 , message = "Description can not exceed 1000 characters")
        String description ,

        @Size(max = 500, message = "Short description cannot exceed 500 characters")
        String shortDescription ,

        @Min (value = 1 , message = "Duration must be greater than 0")
        Integer duration ,

        @DecimalMin(value = "0.0", message = "Price cannot be negative")
        BigDecimal price ,

        @NotNull (message = "Level can not be empty")
        Level level ,

        Status status ,

        @NotNull (message = "The course must have a reference to an instructor")
        Long instructorId ,

        @NotNull (message = "The course must have a refernce to a category")
        Long categoryId

) implements CourseDto {
}
