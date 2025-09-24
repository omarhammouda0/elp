package com.example.demo.repo;

import com.example.demo.entity.course.Course;
import com.example.demo.entity.course.Level;
import com.example.demo.entity.course.Status;
import com.example.demo.entity.user.User;
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
}
