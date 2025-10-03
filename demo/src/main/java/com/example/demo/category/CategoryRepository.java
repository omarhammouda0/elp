package com.example.demo.category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {


    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug , Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String newName , Long id);
}
