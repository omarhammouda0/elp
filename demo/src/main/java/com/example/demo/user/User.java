package com.example.demo.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
@Getter
@Setter

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false , length = 50)
    @NotBlank (message = "User name can not be empty")
    @Size(min = 10, max = 255)
    private String userName;

    @Column(nullable = false, unique = true, length = 255)
    @Email @NotBlank (message = "Email can not be empty")
    @Size(min = 10, max = 255)
    private String email;

    @Column(nullable = false , length = 255)
    @Size(min = 10 , message = "password must be at least 10 characters")
    private String password;

    @Column(nullable = false, length = 60)
    @NotBlank(message = "First name can not be empty")
    private String firstName;

    @Column(nullable = false, length = 60)
    @NotBlank(message = "Last name can not be empty")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column
    private boolean isActive = true;

    @Column(nullable = false , updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
