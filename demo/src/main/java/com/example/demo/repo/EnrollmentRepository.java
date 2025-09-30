package com.example.demo.repo;

import com.example.demo.entity.enrollment.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository

public interface EnrollmentRepository extends JpaRepository<Enrollment,Long> {

    @Query ("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Enrollment e " +
            "WHERE e.user.id=:user_id AND e.course.id=:course_id")

    boolean existsByUserIdAndCourseId(@Param ( "user_id" ) Long userId,
                                      @Param ( "course_id" ) Long courseId);

}
