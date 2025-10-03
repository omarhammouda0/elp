package com.example.demo.course;
import com.example.demo.category.Category;
import com.example.demo.user.User;
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
@Table (name = "course")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column (nullable = false , length = 255)
    private String title;

    @Column
    private String description;

    @Column
    private String shortDescription;

    @Column
    private Integer duration;

    @Column (precision = 10, scale = 2)
    private BigDecimal price;

    @Column (nullable = false)
    @Enumerated(EnumType.STRING)
    private Level level ;

    @Column (nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)

    private User instructor;


    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)

    private Category category;

    @Column (updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt;


}


