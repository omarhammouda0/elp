package com.example.demo.category;
import java.time.LocalDateTime;

public record CategoryResponseDto

        (


                String name ,
                String description ,
                String slug ,
                Boolean isActive ,
                LocalDateTime createdDate


        ) {

}
