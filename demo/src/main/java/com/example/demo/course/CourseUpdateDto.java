package com.example.demo.course;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CourseUpdateDto (


    @Size(min = 5 , max = 255 , message = "The title must be between 5 and 255 characters")
    String title ,

    @Size(max = 1000 , message = "Description can not exceed 1000 characters")
    String description ,

    @Size(max = 500, message = "Short description cannot exceed 500 characters")
    String shortDescription ,

    @Min(value = 1 , message = "Duration must be greater than 0")
    Integer duration ,

    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    BigDecimal price ,

    Level level ,

    Status status ,

    Long instructorId ,

    Long categoryId

) {

}
