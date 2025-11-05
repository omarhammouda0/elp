package com.example.demo.enrollment;

import com.example.demo.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface EnrollmentRepository extends JpaRepository<Enrollment,Long> {

    @Query ("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Enrollment e " +
            "WHERE e.user.id=:user_id AND e.course.id=:course_id")

    boolean existsByUserIdAndCourseId(@Param ( "user_id" ) Long userId,
                                      @Param ( "course_id" ) Long courseId);

    Page<Enrollment> findByUser_Id(Long userId , Pageable pageable);

    Page<Enrollment> findByCourseId(Long courseId , Pageable pageable);


    @Query("SELECT e FROM Enrollment e join Course c " +
            " on e.course.id = c.id" +
            " where c.instructor.id =:instructorId" +
            " order by e.dateOfEnrollment desc "
    )
    Page<Enrollment> findByInstructorId(Long instructorId , Pageable pageable);

    @Query ("select e.course.instructor.email from Enrollment e where e.id = :enrollmentId ")
    String findInstructorEmailByEnrollmentId (@Param ( "enrollmentId" ) Long enrollmentId );

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Enrollment e " +
            "WHERE e.user.id = :userId " +
            "AND e.course.instructor.id = :instructorId")
    boolean existsByUserIdAndInstructorId(@Param("userId") Long userId,
                                          @Param("instructorId") Long instructorId);
}




