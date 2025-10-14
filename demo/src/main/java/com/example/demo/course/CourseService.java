package com.example.demo.course;
import com.example.demo.category.Category;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.exception.model.ErrorCode;
import com.example.demo.exception.types.DuplicateResourceException;
import com.example.demo.exception.types.InvalidRoleException;
import com.example.demo.exception.types.NotFoundException;
import com.example.demo.category.CategoryRepository;
import com.example.demo.module.ModuleRepository;
import com.example.demo.user.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;

    public CourseService(CourseRepository courseRepository , CourseMapper courseMapper ,
                         CategoryRepository categoryRepository , UserRepository userRepository , ModuleRepository moduleRepository) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
    }

    // ________________________Create__________________________

    @Transactional
    public CourseResponseDto createCourse(@Valid CourseCreateDto dto ,
                                          Authentication authentication) {

        Objects.requireNonNull ( dto , "Course is required" );

        courseAccessValidation ( dto, authentication );

        User instructor = validInstructor ( dto.instructorId () );
        Category category = validCategory ( dto.categoryId () );
        String trimmedTitle = validTrimmedTitle ( dto.title () );
        BigDecimal price = validPrice ( dto.price () );
        Status status = validStatus ( dto.status () );



        log.info ( "Creating new course: {}" , trimmedTitle );

        Course toSave = courseMapper.toCourse ( dto );
        toSave.setId ( null );
        toSave.setTitle ( trimmedTitle );
        toSave.setInstructor ( instructor );
        toSave.setCategory ( category );
        toSave.setPrice ( price );
        toSave.setStatus ( status );

        Course savedCourse = courseRepository.save ( toSave );
        return courseMapper.toCourseDto ( savedCourse );
    }

    // ________________________Read__________________________

    @Transactional(readOnly = true)
    public Page<CourseResponseDto> getAllCourses(Pageable pageable) {
        return courseRepository.findAll ( pageable )
                .map ( courseMapper::toCourseDto );
    }

    @Transactional(readOnly = true)
    public CourseResponseDto getCourseById(Long id) {
        Objects.requireNonNull ( id , "id is required" );
        return courseRepository.findById ( id )
                .map ( courseMapper::toCourseDto )
                .orElseThrow ( () -> new NotFoundException (
                        ErrorCode.COURSE_NOT_FOUND.toString ( ) , "Course with id " + id + " not found" ) );
    }

    @Transactional(readOnly = true)
    public CourseResponseDto getCourseByTitle(String title) {
        Objects.requireNonNull ( title , "title is required" );
        String trimmedTitle = title.replaceAll ( "\\s+" , " " ).trim ( );
        if (trimmedTitle.isEmpty ( )) throw new IllegalArgumentException ( "Title cannot be empty" );

        return courseRepository.findByTitleIgnoreCase ( trimmedTitle )
                .map ( courseMapper::toCourseDto )
                .orElseThrow ( () -> new NotFoundException (
                        ErrorCode.COURSE_NOT_FOUND.toString ( ) , "Course with title " + trimmedTitle + " not found" ) );
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getCoursesByCategory(String category) {
        Objects.requireNonNull ( category , "category is required" );
        String trimmedCategory = category.trim ( );
        if (trimmedCategory.isEmpty ( )) throw new IllegalArgumentException ( "Category cannot be empty" );

        List<Course> courses = courseRepository.findByCategoryNameIgnoreCase ( trimmedCategory );
        if (courses.isEmpty ( )) {
            boolean categoryExists = categoryRepository.existsByNameIgnoreCase ( trimmedCategory );
            if (!categoryExists) {
                throw new NotFoundException (
                        ErrorCode.CATEGORY_NOT_FOUND.toString ( ) , "Category " + trimmedCategory + " not found" );
            }
        }
        return courses.stream ( ).map ( courseMapper::toCourseDto ).toList ( );
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getCoursesByInstructor(String instructorName) {
        Objects.requireNonNull ( instructorName , "instructor is required" );
        String s = instructorName.trim ( );
        if (s.isEmpty ( )) throw new IllegalArgumentException ( "Instructor cannot be empty" );

        String[] parts = s.split ( "\\s+" , 2 );
        String firstName = parts[0].trim ( );
        String lastName = (parts.length > 1) ? parts[1].trim ( ) : "";

        User u = userRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase ( firstName , lastName ).orElseThrow (
                () -> new NotFoundException (
                        ErrorCode.INSTRUCTOR_NOT_FOUND.toString ( ) ,
                        "Instructor with name " + firstName + " " + lastName + " not found" )
        );
        if (u.getRole ( ) != Role.INSTRUCTOR) {
            throw new IllegalArgumentException ( "The given user must be an instructor" );
        }

        return courseRepository.findByInstructor ( u ).stream ( ).map ( courseMapper::toCourseDto ).toList ( );
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getCoursesByStatus(String status) {
        Objects.requireNonNull ( status , "status is required" );
        String trimmed = status.trim ( );
        if (trimmed.isEmpty ( )) throw new IllegalArgumentException ( "Status cannot be empty" );

        Status courseStatus = Arrays.stream ( Status.values ( ) )
                .filter ( s -> s.name ( ).equalsIgnoreCase ( trimmed ) )
                .findFirst ( )
                .orElseThrow ( () -> new IllegalArgumentException (
                        "Invalid status '" + status + "'. Must be one of: " +
                                Arrays.stream ( Status.values ( ) ).map ( Enum::name ).collect ( Collectors.joining ( ", " ) ) ) );

        return courseRepository.findAllByStatus ( courseStatus ).stream ( ).map ( courseMapper::toCourseDto ).toList ( );
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getCoursesByLevel(String level) {
        Objects.requireNonNull ( level , "level is required" );
        String trimmed = level.trim ( );
        if (trimmed.isEmpty ( )) throw new IllegalArgumentException ( "Level cannot be empty" );

        Level courseLevel = Arrays.stream ( Level.values ( ) )
                .filter ( l -> l.name ( ).equalsIgnoreCase ( trimmed ) )
                .findFirst ( )
                .orElseThrow ( () -> new IllegalArgumentException (
                        "Invalid level '" + trimmed + "'. Must be one of: " +
                                Arrays.stream ( Level.values ( ) ).map ( Enum::name ).collect ( Collectors.joining ( ", " ) ) ) );

        return courseRepository.findByLevel ( courseLevel ).stream ( ).map ( courseMapper::toCourseDto ).toList ( );
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getFreeCourses() {
        return courseRepository.findFreeCourses ( ).stream ( ).map ( courseMapper::toCourseDto ).toList ( );
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getPaidCourses() {
        return courseRepository.findPaidCourses ( ).stream ( ).map ( courseMapper::toCourseDto ).toList ( );
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getCoursesByPrice(BigDecimal price) {
        Objects.requireNonNull ( price , "price is required" );
        if (price.signum ( ) < 0) throw new IllegalArgumentException ( "Price cannot be negative" );

        BigDecimal normalized = price.setScale ( 2 , RoundingMode.HALF_UP );
        return courseRepository.findByPrice ( normalized ).stream ( ).map ( courseMapper::toCourseDto ).toList ( );
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> findAllByCoursesWithinRange(BigDecimal from , BigDecimal to) {
        Objects.requireNonNull ( from , "from is required" );
        Objects.requireNonNull ( to , "to is required" );
        if (from.signum ( ) < 0 || to.signum ( ) < 0)
            throw new IllegalArgumentException ( "Prices cannot be negative" );
        if (from.compareTo ( to ) > 0)
            throw new IllegalArgumentException ( "Minimum price (" + from + ") must be less than maximum price (" + to + ")" );

        BigDecimal min = from.setScale ( 2 , RoundingMode.DOWN );
        BigDecimal max = to.setScale ( 2 , RoundingMode.UP );

        return courseRepository.findAllByCoursesBetween ( min , max ).stream ( ).map ( courseMapper::toCourseDto ).toList ( );
    }

    // ________________________Update__________________________

    @Transactional
    public CourseResponseDto updateCourse(Long courseId , @Valid CourseUpdateDto dto ,
                                          Authentication authentication) {

        Objects.requireNonNull ( dto , "dto is required" );
        Objects.requireNonNull ( courseId , "course Id is required" );

        Course course = courseRepository.findById ( courseId ).orElseThrow (
                () -> new NotFoundException (
                        ErrorCode.COURSE_NOT_FOUND.toString ( ) , "Course with id " + courseId + " not found" )
        );

        courseUpdateAccessValidation ( course , authentication );

        if (dto.instructorId () != null) {

            var instructor = validInstructor ( dto.instructorId ( ) );
            isAdmin ( authentication );
            course.setInstructor ( instructor );

        }

        if (dto.categoryId () != null) {

            var category = validCategory ( dto.categoryId ( ) );
            course.setCategory ( category );
        }


        if (dto.title () != null) {

            var trimmedTitle = validTrimmedTitle ( dto.title ( ) );
            course.setTitle ( trimmedTitle );
        }


        if (dto.description ( ) != null)
            course.setDescription ( dto.description ( ).replaceAll ( "\\s+" , " " ).trim ( ) );

        if (dto.shortDescription ( ) != null)
            course.setShortDescription ( dto.shortDescription ( ).replaceAll ( "\\s+" , " " ).trim ( ) );

        if (dto.duration ( ) != null) {
            if (dto.duration ( ) < 1) throw new IllegalArgumentException ( "Duration must be greater than 0" );
            course.setDuration ( dto.duration ( ) );
        }

        if (dto.price () != null) {

            var newPrice = validPrice ( dto.price ( ) );
            var oldPrice = course.getPrice ();

            if (!oldPrice.equals ( newPrice )) {
                log.warn ( "Price for the course {} changed from {} to {} by user {} " ,
                        courseId , oldPrice , newPrice , authentication.getName ( ) );
            }

            course.setPrice ( newPrice );

        }


        if (dto.level ( ) != null)
            course.setLevel ( dto.level ( ) );

        if (dto.status ( ) != null) {

            if (dto.status ( ) == Status.ARCHIVED) {
                throw new IllegalArgumentException ( "Use the archive endpoint to archive a course" );
            }

            if (dto.status ( ) == Status.PUBLISHED) {
                if (course.getStatus ( ) == Status.ARCHIVED) {
                    throw new IllegalArgumentException ( "Cannot publish an archived course" );
                }

                if (course.getStatus ( ) == Status.DRAFT) {
                    if (!course.getCategory ( ).isActive ( )) {
                        throw new IllegalArgumentException ( "Can not publish to an inactive category" );
                    }
                }

            }

            if (dto.status ( ) == Status.DRAFT) {

                if (course.getStatus ( ) == Status.PUBLISHED) {
                    throw new IllegalArgumentException ( "Cannot draft an published course" );
                }
            }
            course.setStatus ( dto.status ( ) );
        }

        log.info ( "Updating course ID: {}" , courseId );

        Course updatedCourse = courseRepository.save ( course );
        return courseMapper.toCourseDto ( updatedCourse );
    }

    // ________________________Archive__________________________

    @Transactional
    public void archiveCourse(Long courseId , Authentication authentication) {
        Objects.requireNonNull ( courseId , "course Id is required" );

        Course course = archiveCourseValidation ( courseId , authentication );
        log.info ( "Archiving course ID: {}" , courseId );

        course.setStatus ( Status.ARCHIVED );
        courseRepository.save ( course );

    }


    private  void courseAccessValidation(CourseCreateDto dto , Authentication authentication) {

        User currentUser = userRepository.findByEmailIgnoreCase ( authentication.getName ( ) ).orElseThrow (
                () -> new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString ( ) ,
                        "User with id: " + authentication.getName ( ) + " not found"
                ) );

        if (currentUser.getRole ( ) == Role.INSTRUCTOR
                && !dto.instructorId ( ).equals ( currentUser.getId ( ) )) {

            log.warn ( "Instructor with ID: {} attempted to manage course  which they do not own" ,
                    currentUser.getId ( ) );

            throw new SecurityException ( "Instructors can only manage their own courses" );
        }

        if (!currentUser.isActive ( )) {

            log.warn ( "Inactive user with ID: {} attempted to manage a course " ,
                    currentUser.getId ( ) );

            throw new SecurityException ( "Inactive users cannot manage courses" );
        }


    }

    private void courseUpdateAccessValidation
            (Course course , Authentication authentication) {

        var currentUser = userRepository.findByEmailIgnoreCase ( authentication.getName ( ) ).orElseThrow (
                () -> new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString ( ) ,
                        "User with id: " + authentication.getName ( ) + " not found"
                ) );

        if (currentUser.getRole ( ) == Role.ADMIN)
            return;

        Long courseId = course.getId ( );

        if (!currentUser.isActive ( )) {

            log.warn ( "Inactive user with the ID: {} attempted to manage a course {} " ,
                    currentUser.getId ( ) , courseId );

            throw new SecurityException ( "Inactive users cannot manage courses" );
        }


        if (!course.getInstructor ( ).getId ( ).equals ( currentUser.getId ( ) )) {

            log.warn ( "Instructor with ID: {} attempted to update course ID: {} which they do not own" ,
                    currentUser.getId ( ) , courseId );

            throw new SecurityException ( "Instructors can only update their own courses" );
        }

    }


    private void isAdmin(Authentication authentication) {

        var currentUser = userRepository.findByEmailIgnoreCase ( authentication.getName ( ) ).orElseThrow (
                () -> new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString ( ) ,
                        "User with id: " + authentication.getName ( ) + " not found"
                ) );

        if (currentUser.getRole ( ) != Role.ADMIN) {
            throw new SecurityException ( "Only admins can perform this action" );
        }
    }

    private User validInstructor(Long id) {

        User instructor = userRepository.findById ( id ).orElseThrow (
                () -> new NotFoundException ( ErrorCode.INSTRUCTOR_NOT_FOUND.toString ( ) , "Instructor not found" )
        );
        if (instructor.getRole ( ) != Role.INSTRUCTOR)

            throw new InvalidRoleException ( ErrorCode.USER_IS_NOT_AN_INSTRUCTOR.toString ( ) ,
                    "The user with id " + id +
                            " is not an instructor , please enter a valid User !" );

        if (!instructor.isActive ( ))
            throw new IllegalArgumentException ( "The instructor is not more active" );

        return instructor;
    }

    private Category validCategory(Long id) {

        Category category = categoryRepository.findById ( id ).orElseThrow (
                () -> new NotFoundException ( ErrorCode.CATEGORY_NOT_FOUND.toString ( ) , "Category not found" )
        );
        if (!category.isActive ( ))
            throw new IllegalArgumentException ( "The category is not more active" );

        return category;
    }

    private String validTrimmedTitle(String title) {

        String trimmedTitle = title.replaceAll ( "\\s+" , " " ).trim ( );

        if (trimmedTitle.isEmpty ( )) throw new IllegalArgumentException ( "Title cannot be empty" );

        if (courseRepository.existsByTitleIgnoreCase ( trimmedTitle )) {
            throw new DuplicateResourceException (
                    ErrorCode.TITLE_ALREADY_EXISTS.toString ( ) ,
                    "A course with this title already exists"
            );
        }

        return trimmedTitle;
    }

    private BigDecimal validPrice(BigDecimal price) {

        if (price == null)
            return BigDecimal.ZERO;

        else if (price.signum ( ) < 0)
            throw new IllegalArgumentException ( "Price cannot be negative" );

        return price.setScale ( 2 , RoundingMode.HALF_UP );

    }

    private Status validStatus(Status status) {
        if (status == null) {
            return Status.DRAFT;
        }
        return status;
    }

    private Course archiveCourseValidation(Long id , Authentication authentication) {

        var currentUser = userRepository.findByEmailIgnoreCase ( authentication.getName ( ) ).orElseThrow (
                () -> new NotFoundException ( ErrorCode.USER_NOT_FOUND.toString ( ) ,
                        "User with id: " + authentication.getName ( ) + " not found"
                ) );

        if (!currentUser.getRole ( ).equals ( Role.ADMIN )) {
            throw new SecurityException ( "Only admins can archive courses" );
        }

        var course = courseRepository.findById ( id ).orElseThrow (
                () -> new NotFoundException (
                        ErrorCode.COURSE_NOT_FOUND.toString ( ) , "Course with id " + id + " not found" )
        );

        if (course.getStatus ( ) == Status.ARCHIVED) {
            throw new IllegalStateException ( "Course is already archived" );
        }

        if (moduleRepository.findByCourseIdAndIsActive ( course.getId ( ) ).isPresent ( )) {
            throw new com.example.demo.exception.types.IllegalArgumentException (
                    ErrorCode.COURSE_HAS_ACTIVE_MODULES.toString ( ) ,
                    "Cannot archive course with active modules. Please deactivate or remove active modules first."
            );
        }
        return course;
    }
}




