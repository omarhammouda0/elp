package com.example.demo.module;

import java.time.LocalDateTime;

public record ModuleResponseDto(

        Long id ,

        String title ,

        String description ,

        Integer orderIndex ,

        Boolean isActive ,

        String courseNAme,

        LocalDateTime createdAt


) {
}
