package com.example.demo.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok ( categoryService.getCategoryById ( id ) );
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> saveCategory (@Valid @RequestBody CategoryCreateDto categoryCreateDto) {
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( categoryService.createCategory (categoryCreateDto) );
    }

    @PutMapping ("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory (@Valid @PathVariable Long id, @RequestBody CategoryCreateDto categoryCreateDto) {
        return ResponseEntity.ok ( categoryService.updateCategory ( id , categoryCreateDto) );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveCategory(@PathVariable Long id) {
        categoryService.archiveCategory (id);
        return ResponseEntity.noContent().build();
    }

}
