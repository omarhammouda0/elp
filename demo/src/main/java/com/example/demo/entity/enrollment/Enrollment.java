package com.example.demo.entity.enrollment;


import com.example.demo.entity.course.Course;
import com.example.demo.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" )
    private User user ;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course ;

    @Column
    private LocalDateTime dateOfEnrollment = LocalDateTime.now() ;

    @Column
    private Boolean isActive  ;

    @Column
    @Enumerated (EnumType.STRING)
    private Progress progress  ;

    @Column
    private LocalDateTime completionDate ;

    @Column
    private BigDecimal finalGrade ;

    @Column
    @CreationTimestamp
    private LocalDateTime createdAt ;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt ;

}
