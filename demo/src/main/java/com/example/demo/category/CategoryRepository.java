package com.example.demo.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface CategoryRepository extends JpaRepository<Category,Long> {


    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug , Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String newName , Long id);

    @Query ("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.id ASC")
    Page<Category> findByActiveOrderById(Pageable pageable);

    @Query ("SELECT c FROM Category c WHERE c.isActive = false ORDER BY c.id ASC")
    Page <Category> findByIsActiveFalseOrderById(Pageable pageable);

    @Query ("SELECT COUNT(c) FROM Course c WHERE c.category.id = :categoryId")
    int findCountOfCoursesInCategory(@Param("categoryId") Long categoryId);


    @Query(value = """ 
            select t1.*
                        from category t1
                        left join course t2
                        on t1.id = t2.category_id
                        where t2.id is null""",

            nativeQuery = true)

    Page <Category> findCategoriesWithNoCourses(Pageable pageable);
}

