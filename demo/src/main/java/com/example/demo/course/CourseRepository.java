package com.example.demo.course;

import com.example.demo.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository

public interface CourseRepository extends JpaRepository<Course, Long> {


    boolean existsByTitleIgnoreCase(String title);

    @EntityGraph(attributePaths = {"instructor","category"})
    Optional<Course> findByTitleIgnoreCase(String title);


    @EntityGraph(attributePaths = {"instructor","category"})
    @Query("select c from Course c where lower(c.category.name) = lower(:name)")
    List<Course> findByCategoryNameIgnoreCase(@Param("name") String name);


    @EntityGraph(attributePaths = {"instructor","category"})
    List<Course> findByInstructor(User instructor);


    @EntityGraph(attributePaths = {"instructor","category"})
    List<Course> findAllByStatus(Status status);


    @EntityGraph(attributePaths = {"instructor","category"})
    List<Course> findByLevel(Level level);


    @EntityGraph(attributePaths = {"instructor","category"})
    @Query("select c from Course c where c.price = 0 or c.price is null")
    List<Course> findFreeCourses();


    @EntityGraph(attributePaths = {"instructor","category"})
    @Query("select c from Course c where c.price > 0")
    List<Course> findPaidCourses();


    @EntityGraph(attributePaths = {"instructor","category"})
    List<Course> findByPrice(BigDecimal price);

    @EntityGraph(attributePaths = {"instructor","category"})
    @Query("select c from Course c where c.price between :min and :max")
    List<Course> findAllByCoursesBetween(@Param("min") BigDecimal min,
                                         @Param("max") BigDecimal max);


    boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);

    List<Course> findByCategoryId(Long categoryId);

    @Query ("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Course c, Enrollment e " +
            "WHERE c.id = e.course.id " +
            "AND c.instructor.email = :instructor_email " +
            "AND e.user.email = :student_email"
    )
    boolean existsByInstructorAndStudent (@Param ( "instructor_email" ) String instructorEmail ,
                                          @Param ( "student_email" ) String userEmail);


    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
                    "FROM Course c " +
                    "WHERE c.instructor.id = :instructorId " +
                    "AND c.status in ('PUBLISHED' , 'DRAFT')"
    )
    boolean activeCoursesForTheInstructor(Long instructorId);


  @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Course c " +
            "WHERE c.category.id = :categoryId " +
            "AND (c.status = 'PUBLISHED' OR c.status = 'DRAFT')")

    boolean hasActiveOrDraftCourses(@Param("categoryId") Long categoryId);

}
