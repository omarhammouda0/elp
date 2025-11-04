package com.example.demo.category;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component

public class CategoryMapper {

    public CategoryResponseDto toResponseDto(Category category) {


        return new CategoryResponseDto(

                category.getId () ,
                category.getName () ,
                category.getDescription () ,
                category.getSlug () ,
                category.isActive (),
                category.getCreatedDate ()
        );
    }

    public Category toCategory(CategoryCreateDto categoryCreateDto) {

        Objects.requireNonNull ( categoryCreateDto, "Category is required" );

        return new Category(

                categoryCreateDto.name ().trim (),
                categoryCreateDto.description() != null  ?
                        categoryCreateDto.description().trim() : null
        );
    }

}
