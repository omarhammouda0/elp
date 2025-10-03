package com.example.demo.enrollment;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Objects;

@Component

public class EnrollmentMapper {


    public Enrollment toEnrollment(EnrollmentCreateDto dto) {

        Objects.requireNonNull ( dto , "Enrollment cannot be null" );
        return Enrollment.builder ( )
                .dateOfEnrollment (  LocalDateTime.now ( ) )
                .isActive ( true )
                .progress ( Progress.NOT_STARTED )
                .finalGrade ( null )
                .completionDate ( null )
                .build ( );
    }

    public EnrollmentResponseDto toEnrollmentResponseDto(Enrollment e) {

        Objects.requireNonNull(e, "Enrollment cannot be null");

        return new EnrollmentResponseDto(

                e.getId (),
                e.getUser ().getFirstName () + " " + e.getUser ().getLastName (),
                e.getCourse ().getTitle (),
                e.getDateOfEnrollment (),
                e.getIsActive (),
                e.getProgress (),
                e.getCompletionDate (),
                e.getFinalGrade ()

        );

    }

}
