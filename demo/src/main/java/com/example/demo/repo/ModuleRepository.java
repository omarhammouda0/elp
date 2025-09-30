package com.example.demo.repo;
import com.example.demo.entity.module.Module;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository

public interface ModuleRepository extends JpaRepository<Module,Long> {


    @Query("select m from Module m order by m.course.id asc , m.orderIndex asc ")
    Page<Module> findAllModulesOrdered(Pageable pageable);

    boolean existsByTitleIgnoreCaseAndCourseId(String title , Long courseId);

    @Query("select max (m.orderIndex) from Module m where m.course.id =:id ")
    Integer findLastOrderIndexByCourseId (@Param("id") Long id);

    @Query ("select m from Module m where m.course.id =:course_id" +
            " order by  m.orderIndex asc , m.isActive ")
    Page<Module> findByCourseId(@Param ( "course_id" ) Long courseId , Pageable pageable);

    boolean existsByTitleIgnoreCaseAndCourseIdAndIdNot(String trimmedTitle , Long courseId , Long id  );


    List<Module> findByCourseIdAndIsActiveTrueOrderByOrderIndexAsc(Long courseId);

    @Query ("select distinct m.course.id from Module m where m.course.id =:course_id and " +
            "m.isActive =true")
    Optional<java.lang.Module> findByCourseIdAndIsActive(@Param ( "course_id" ) Long courseId );


}
