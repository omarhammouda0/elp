package com.example.demo.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record CategoryCreateDto(

        @NotBlank (message = "Name can not be empty")
        @Size (min = 3 , max = 60)
        String name ,

        @Size( max = 255 , message = "Description must be between 3 and 255 characters")
        String description ,

        Boolean isActive )



        implements Serializable {


}
