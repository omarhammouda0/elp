package com.example.demo.course;
import java.math.BigDecimal;


public record CourseResponseDto (

        Long id,
        String title ,
        String description ,
        String short_description ,
        Integer duration ,
        BigDecimal price ,
        Level level ,
        Status status ,
        String instructorName ,
        String categoryName


){
}
