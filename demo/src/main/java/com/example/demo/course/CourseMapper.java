package com.example.demo.course;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component

public class CourseMapper {

    public CourseResponseDto toCourseDto (Course course) {

        Objects.requireNonNull ( course , "course can not be null");

        String instructorName = "Unknown";
        if (course.getInstructor ( ) != null) {
            String first = course.getInstructor ( ).getFirstName ( );
            String last = course.getInstructor ( ).getLastName ( );
            instructorName = ((first != null ? first : "") + " " +
                    (last != null ? last : "")).trim ( );
        }

        String categoryName = course.getCategory() != null ?
                course.getCategory().getName() : "Unknown";

        return new CourseResponseDto(

                course.getId (),
                course.getTitle (),
                course.getDescription (),
                course.getShortDescription (),
                course.getDuration (),
                course.getPrice (),
                course.getLevel (),
                course.getStatus (),
                instructorName,
                categoryName

        );
    }

    public Course toCourse(CourseCreateDto dto) {


        Objects.requireNonNull ( dto, "Course can not be null");

        return Course.builder ()

                .description(dto.description() != null ? dto.description().trim() : null)
                .shortDescription(dto.shortDescription() != null ? dto.shortDescription().trim() : null)
                .duration(dto.duration())
                .price(dto.price())
                .level(dto.level())
                .status(dto.status() != null ? dto.status() : Status.DRAFT)
                .build();
    }
}
