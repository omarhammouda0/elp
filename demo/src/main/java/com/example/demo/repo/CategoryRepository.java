package com.example.demo.repo;

import com.example.demo.entity.category.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {


    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug , Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String newName , Long id);
}
