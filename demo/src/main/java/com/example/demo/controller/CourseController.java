package com.example.demo.controller;

import com.example.demo.entity.course.CourseCreateDto;
import com.example.demo.entity.course.CourseResponseDto;
import com.example.demo.entity.course.CourseUpdateDto;
import com.example.demo.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<Page<CourseResponseDto>> getAllCourses(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(courseService.getAllCourses(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok ( courseService.getCourseById ( id ) );
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<CourseResponseDto> getCoursesByTitle(@PathVariable String title) {
        return ResponseEntity.ok ( courseService.getCourseByTitle ( title ) );
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByCategory(@PathVariable String category) {
        return ResponseEntity.ok ( courseService.getCoursesByCategory ( category ) );
    }

    @GetMapping("/instructor/{instructor}")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByInstructor(@PathVariable String instructor) {
        return ResponseEntity.ok ( courseService.getCoursesByInstructor ( instructor ) );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByStatus(@PathVariable String status) {
        return ResponseEntity.ok ( courseService.getCoursesByStatus ( status ) );
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByLevel(@PathVariable String level) {
        return ResponseEntity.ok ( courseService.getCoursesByLevel ( level ) );
    }

    @GetMapping("/free")
    public ResponseEntity<List<CourseResponseDto>> getFreeCourses( ) {
        return ResponseEntity.ok ( courseService.getFreeCourses (  ) );
    }

    @GetMapping("/paid")
    public ResponseEntity<List<CourseResponseDto>> getPaidCourses( ) {
        return ResponseEntity.ok ( courseService.getPaidCourses (  ) );
    }

    @GetMapping("/price/{price}")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByPrice( @PathVariable BigDecimal price ) {
        return ResponseEntity.ok ( courseService.getCoursesByPrice ( price ) );
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<CourseResponseDto>> findAllByCoursesWithinRange(
            @RequestParam BigDecimal from,
            @RequestParam BigDecimal to) {
        return ResponseEntity.ok(courseService.findAllByCoursesWithinRange(from, to));
    }

    @PostMapping
    public ResponseEntity <CourseResponseDto> createCourse (@Valid @RequestBody CourseCreateDto dto) {
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( courseService.createCourse (dto) );
    }

    @PutMapping ("/{id}")
    public ResponseEntity <CourseResponseDto> updateCourse( @PathVariable Long id ,
                                                            @Valid @RequestBody CourseUpdateDto dto) {

        return ResponseEntity.ok ( courseService.updateCourse ( id , dto ) );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity <Void> deleteCourse(@PathVariable Long id) {
        courseService.archiveCourse (id);
        return ResponseEntity.noContent().build();
    }


}
