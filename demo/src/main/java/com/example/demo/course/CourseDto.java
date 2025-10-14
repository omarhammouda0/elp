package com.example.demo.course;

import java.math.BigDecimal;

public interface CourseDto {
    String title();
    String description();
    String shortDescription();
    Integer duration();
    BigDecimal price();
    Level level();
    Status status();
    Long instructorId();
    Long categoryId();
}