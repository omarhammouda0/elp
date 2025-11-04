package com.example.demo.category;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString @EqualsAndHashCode
@Builder

@Entity
@Table(name = "category", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name"),
        @UniqueConstraint(columnNames = "slug")
})


public class Category {


    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @Column( nullable = false, unique = true , length = 60)
    private String name;

    @Column( length = 255)
    private String description;

    @Column( unique = true, length = 255)
    private String slug;

    @Column
    private boolean isActive = true;

    @Column(  name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdDate;

    @Column (name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedDate;

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

