package com.example.demo.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")

public class CategoryController  {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<Page <CategoryResponseDto> > getAllCategories(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {

        return ResponseEntity.ok ( categoryService.getCategories (pageable) );
    }

    @GetMapping("/active")
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity <Page <CategoryResponseDto> > getAllActiveCategories(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {

        return ResponseEntity.ok ( categoryService.getActiveCategories (pageable) );
    }


    @GetMapping("/inactive")
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity <Page <CategoryResponseDto> > getAllInActiveCategories(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {

        return ResponseEntity.ok ( categoryService.getInActiveCategories (pageable) );
    }


    @GetMapping("/courses_count/{categoryId}")
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity<Integer> getCoursesCountForCategory(@PathVariable Long categoryId) {
        int count = categoryService.getCoursesCountForCategory(categoryId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/with_no_courses")
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity <Page<CategoryResponseDto>> findCategoriesWithNoCourses(Pageable pageable){
        return ResponseEntity.ok ( categoryService.findCategoriesWithNoCourses (pageable) );
    }



    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok ( categoryService.getCategoryById ( id ) );
    }


    @PostMapping
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity<CategoryResponseDto> saveCategory (@Valid @RequestBody CategoryCreateDto categoryCreateDto) {
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( categoryService.createCategory (categoryCreateDto) );
    }

    @PutMapping ("/{id}")
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity<CategoryResponseDto> updateCategory (@PathVariable Long id,
                                                               @Valid @RequestBody CategoryCreateDto categoryCreateDto) {
        return ResponseEntity.ok ( categoryService.updateCategory ( id , categoryCreateDto) );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity<Void> archiveCategory(@PathVariable Long id) {
        categoryService.archiveCategory (id);
        return ResponseEntity.noContent().build();
    }

}
