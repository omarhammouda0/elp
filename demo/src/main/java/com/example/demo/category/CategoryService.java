package com.example.demo.category;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.example.demo.course.Status;
import com.example.demo.exception.types.InvalidOperationException;
import com.example.demo.course.CourseRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.exception.model.ErrorCode;
import com.example.demo.exception.types.DuplicateResourceException;
import com.example.demo.exception.types.NotFoundException;

@Service

public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private  final CourseRepository courseRepository;

    public CategoryService(CategoryRepository categoryRepository , CategoryMapper categoryMapper , CourseRepository courseRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.courseRepository = courseRepository;
    }

    private String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        return name.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("^-+|-+$", "");

    }

    @Transactional (readOnly = true)
    public Page <CategoryResponseDto > getCategories(Pageable pageable)
    {
        return categoryRepository.findAll ( pageable )
                .map ( categoryMapper::toResponseDto )
                ;
    }

    @Transactional (readOnly = true)
    public Page <CategoryResponseDto > getActiveCategories(Pageable pageable)
    {
        return categoryRepository.findByActiveOrderById ( pageable )
                .map ( categoryMapper::toResponseDto )
                ;
    }

    @Transactional (readOnly = true)
    public Page<CategoryResponseDto> getInActiveCategories(Pageable pageable) {

        return categoryRepository.findByIsActiveFalseOrderById(pageable)
                .map(categoryMapper::toResponseDto);
    }

    @Transactional (readOnly = true)
    public Page<CategoryResponseDto> findCategoriesWithNoCourses(Pageable pageable) {
        return categoryRepository.findCategoriesWithNoCourses(pageable)
                .map(categoryMapper::toResponseDto);
    }

    @Transactional (readOnly = true)
    public int getCoursesCountForCategory(Long id) {

        Objects.requireNonNull ( id , "Category id is required" );

        if (!categoryRepository.existsById ( id ))
            throw new NotFoundException (
                    ErrorCode.CATEGORY_NOT_FOUND.toString ( ) ,
                    "Category with id " + id + " not found"
            );

        return categoryRepository.findCountOfCoursesInCategory ( id );

    }

    @Transactional (readOnly = true)
    public CategoryResponseDto getCategoryById(Long id)
    {
        Objects.requireNonNull(id, "id is required");
        return categoryRepository.findById(id)
                .map ( categoryMapper::toResponseDto )
                .orElseThrow (  () ->
                new NotFoundException ( ErrorCode.CATEGORY_NOT_FOUND.toString () ,
                        "Category with id " + id + " not found") );

    }

    @Transactional
    public CategoryResponseDto createCategory(@Valid CategoryCreateDto categoryCreateDto) {

        Objects.requireNonNull ( categoryCreateDto , "Category is required" );
        Category category = categoryMapper.toCategory ( categoryCreateDto );

        String trimmedName = category.getName().trim();
        category.setName( trimmedName );

        String slug = generateSlug(trimmedName);
        category.setSlug(slug);

        if (categoryRepository.existsByNameIgnoreCase ( trimmedName )) {
            throw new DuplicateResourceException
                    ( ErrorCode.CATEGORY_ALREADY_EXISTS.toString ( ) ,
                            "Category with name " + trimmedName + " already exists" );
        }

        if (categoryRepository.existsBySlug ( category.getSlug ( ) )) {
            throw new DuplicateResourceException (
                    ErrorCode.SLUG_ALREADY_EXISTS.toString ( ) ,
                    "Category with slug " + category.getSlug ( ) + " already exists" );
        }

        Category savedCategory = categoryRepository.save ( category );
        return categoryMapper.toResponseDto ( savedCategory );
    }

    @Transactional
    public CategoryResponseDto updateCategory(Long id , @Valid CategoryCreateDto categoryCreateDto) {

        Objects.requireNonNull ( categoryCreateDto , "Category is required" );
        Objects.requireNonNull ( id , "id is required" );

        Category categoryToUpdate = categoryRepository.findById ( id )
                .orElseThrow ( () -> new NotFoundException (
                        ErrorCode.CATEGORY_NOT_FOUND.toString ( ) , "Category with id " + id + " not found"
                ) );

        if (categoryCreateDto.name ( ) != null && !categoryCreateDto.name ( ).trim ( ).isEmpty ( )) {

            String newName = categoryCreateDto.name ( ).trim ( );

            if (categoryRepository.existsByNameIgnoreCaseAndIdNot ( newName , categoryToUpdate.getId ( ) ))
                throw new DuplicateResourceException (
                        ErrorCode.CATEGORY_ALREADY_EXISTS.toString ( ) ,
                        "Category with name " + newName + " already exists"
                );

            String newSlug = generateSlug ( newName );

            if (categoryRepository.existsBySlugAndIdNot(newSlug, id)) {
                newSlug = newSlug + "-" + UUID.randomUUID().toString().substring(0, 8);
            }

            categoryToUpdate.setName ( newName );
            categoryToUpdate.setSlug ( newSlug );
        }

        if (categoryCreateDto.description ( ) != null) {
            categoryToUpdate.setDescription ( categoryCreateDto.description ( ).trim ( ) );
        }

        if (categoryCreateDto.isActive ( ) != null) {

            if (!categoryCreateDto.isActive ( )) {
                throw new InvalidOperationException (
                        ErrorCode.CANNOT_DEACTIVATE_CATEGORY_THROUGH_UPDATE_METHOD_PLEASE_USE_ARCHIVE_METHOD_INSTEAD
                                .toString ( ) ,
                        " Please use archiveCategory method instead !"
                );

            }

            categoryToUpdate.setActive ( true );

        }

        Category updatedCategory = categoryRepository.save ( categoryToUpdate );
        return categoryMapper.toResponseDto ( updatedCategory );
    }

    @Transactional
        public void archiveCategory(Long id)
    {
        Objects.requireNonNull (id, "Category id is required");

        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException(
                        ErrorCode.CATEGORY_NOT_FOUND.toString(), "Category with id " + id + " not found")
        );

        if (courseRepository.hasActiveOrDraftCourses ( category.getId () ))
        {
            throw new com.example.demo.exception.types.IllegalStateException (
                    ErrorCode.CATEGORY_HAS_ACTIVE_COURSES.toString (),
                    "Category with id " + id + " has active or draft courses. Cannot archive."
            );
        }

        category.setActive(false);
        categoryRepository.save(category);
    }



}
