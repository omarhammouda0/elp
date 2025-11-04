package com.example.demo.module;

import com.example.demo.course.Course;
import com.example.demo.course.Status;
import com.example.demo.exception.model.ErrorCode;
import com.example.demo.exception.types.DuplicateResourceException;
import com.example.demo.exception.types.NotFoundException;
import com.example.demo.course.CourseRepository;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserCreationDto;
import com.example.demo.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;
    private final CourseRepository courseRepository;
    private  final UserRepository userRepository;


//     ________________________Create__________________________

    @Transactional
    public ModuleResponseDto createModule(@Valid ModuleCreationDto dto ,
                                          Authentication authentication) {

        Objects.requireNonNull ( dto , "module cannot be null" );

        var curruntUser = getCurrentUser ( authentication );
        validateCurrentUserActivation (  curruntUser );
        validateModuleOwnershipForCreate ( dto , curruntUser );


        Long courseId = dto.courseId ( );

        Integer lastOrderIndex = moduleRepository.findLastOrderIndexByCourseId ( courseId );
        Integer orderIndex = lastOrderIndex == null ? 1 : lastOrderIndex + 1;

        Course existingCourse = courseRepository.findById ( courseId ).orElseThrow (
                () -> new NotFoundException (
                        ErrorCode.COURSE_NOT_FOUND.toString ( ) ,
                        "The course is not found , please enter a valid course"
                )
        );

        String trimmedTitle = dto.title ( ).replaceAll ( "\\s+" , " " ).trim ( );

        if (trimmedTitle.isEmpty ( )) {
            throw new IllegalArgumentException ( "Title can not be empty" );
        }

        if (moduleRepository.existsByTitleIgnoreCaseAndCourseId ( trimmedTitle , courseId ))
            throw new DuplicateResourceException (
                    ErrorCode.MODULE_ALREADY_EXISTS.toString ( ) ,
                    "Module with this title already exists , please add another title "
            );

        if (existingCourse.getStatus ().equals ( Status.ARCHIVED )) {
            throw new IllegalStateException ( "Cannot add module to an archived course. " +
                    "Please publish the course first." );
        }

        Module toSave = moduleMapper.toModule ( dto );
        toSave.setCourse ( existingCourse );
        toSave.setOrderIndex ( orderIndex );

        Module savedModule = moduleRepository.save ( toSave );
        return moduleMapper.toModuleResponseDto ( savedModule );


    }

    //     ________________________Read__________________________


    @Transactional(readOnly = true)
    public Page<ModuleResponseDto> getAllModulesOrdered(Pageable pageable) {

        return moduleRepository.findAllModulesOrdered ( pageable )
                .map ( moduleMapper::toModuleResponseDto  );

    }

    @Transactional(readOnly = true)
    public ModuleResponseDto getModuleById(Long id) {
        Objects.requireNonNull ( id , "id is required" );
        return moduleRepository.findById ( id )
                .map ( moduleMapper::toModuleResponseDto )
                .orElseThrow ( () -> new NotFoundException (
                        ErrorCode.MODULE_NOT_FOUND.toString ( ) , "Module with the id " + id + " not found" ) );
    }

    @Transactional(readOnly = true)
    public Page<ModuleResponseDto> getModulesByCourseId(Long courseId , Pageable pageable) {

        Objects.requireNonNull ( courseId , "courseId cannot be null" );

        courseRepository.findById ( courseId ).orElseThrow (
                () -> new NotFoundException (

                        ErrorCode.COURSE_NOT_FOUND.toString ( ) ,
                        "The course is not found , please enter a valid course"
                )
        );

        return moduleRepository.findByCourseId ( courseId , pageable )
                .map ( moduleMapper::toModuleResponseDto
                );

    }

    // ________________________Update__________________________


    @Transactional
    public ModuleResponseDto updateModule(Long id,
                                          @Valid ModuleUpdateDto dto ,
                                          Authentication authentication)

    {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(dto, "module cannot be null");

        Module toUpdate = moduleRepository.findById(id).orElseThrow(() ->
                new NotFoundException(ErrorCode.MODULE_NOT_FOUND.toString(),
                        "Module with id " + id + " not found"));

        var curruntUser = getCurrentUser ( authentication );
        validateCurrentUserActivation (  curruntUser );
        validateModuleOwnershipForUpdate ( id , curruntUser );


        Long originalCourseId = toUpdate.getCourse().getId();
        boolean courseChanged = false;
        boolean orderChanged = false;
        boolean reactivated = false;

        if (dto.title() != null) {
            String trimmedTitle = dto.title().replaceAll("\\s+", " ").trim();
            if (trimmedTitle.isEmpty()) {
                throw new IllegalArgumentException("Title cannot be empty");
            }

            Long courseIdToCheck = dto.courseId() != null ? dto.courseId() : originalCourseId;

            if (moduleRepository.existsByTitleIgnoreCaseAndCourseIdAndIdNot(
                    trimmedTitle, courseIdToCheck, id)) {
                throw new DuplicateResourceException(
                        ErrorCode.TITLE_ALREADY_EXISTS.toString(),
                        "A module with this title already exists in this course");
            }
            toUpdate.setTitle(trimmedTitle);
        }

        if (dto.description() != null) {
            String trimmedDescription = dto.description().replaceAll("\\s+", " ").trim();
            toUpdate.setDescription(trimmedDescription.isEmpty() ? null : trimmedDescription);
        }

        if (dto.courseId() != null && !dto.courseId().equals(originalCourseId)) {

           if (! curruntUser.getRole ().equals ( Role.ADMIN )) {
               throw new SecurityException (
                       "Only admins can move modules between courses"
               );
           }

            Course newCourse = courseRepository.findById(dto.courseId())
                    .orElseThrow(() -> new NotFoundException(
                            ErrorCode.COURSE_NOT_FOUND.toString(),
                            "Course with id " + dto.courseId() + " not found"));


            toUpdate.setCourse(newCourse);
            courseChanged = true;
        }

        if (dto.orderIndex() != null) {
            orderChanged = true;
            toUpdate.setOrderIndex(999999);
        }

        if (dto.isActive() != null) {
            if (!dto.isActive()) {
                throw new IllegalArgumentException("Please use the delete endpoint");
            }


            if (!toUpdate.getIsActive()) {
                toUpdate.setIsActive(true);
                reactivated = true;
            }
        }

        Module updatedModule = moduleRepository.save(toUpdate);


        if (courseChanged) {
            reorderModulesInCourse(originalCourseId);
            if (orderChanged) {
                insertModuleAtPosition(updatedModule, dto.orderIndex());
            } else {
                reorderModulesInCourse(updatedModule.getCourse().getId());
            }
        } else if (orderChanged) {
            insertModuleAtPosition(updatedModule, dto.orderIndex());
        } else if (reactivated) {

            reorderModulesInCourse(updatedModule.getCourse().getId());
        }

        log.info("Updating module with ID: {}", id);
        return moduleMapper.toModuleResponseDto(updatedModule);
    }

    private void insertModuleAtPosition(Module module, Integer targetPosition) {

        List<Module> activeModules = moduleRepository.findByCourseIdAndIsActiveTrueOrderByOrderIndexAsc(
                module.getCourse().getId());


        activeModules.removeIf(m -> m.getId().equals(module.getId()));

        int insertIndex = Math.max(0, Math.min(targetPosition - 1, activeModules.size()));
        activeModules.add(insertIndex, module);

        for (int i = 0; i < activeModules.size(); i++) {
            activeModules.get(i).setOrderIndex(i + 1);
        }
        moduleRepository.saveAll(activeModules);
    }

    private void reorderModulesInCourse(Long courseId) {
        List<Module> activeModules = moduleRepository.findByCourseIdAndIsActiveTrueOrderByOrderIndexAsc(courseId);
        for (int i = 0; i < activeModules.size(); i++) {
            activeModules.get(i).setOrderIndex(i + 1);
        }
        moduleRepository.saveAll(activeModules);
    }

    // ________________________Delete__________________________

    @Transactional
    public void archiveModule(Long moduleId , Authentication authentication) {

        Objects.requireNonNull ( moduleId , "Module Id is required" );

        Module module = moduleRepository.findById ( moduleId ).orElseThrow (
                () -> new NotFoundException (
                        ErrorCode.MODULE_NOT_FOUND.toString ( ) ,
                        "Module with id " + moduleId + " not found" ) );


        var curruntUser = getCurrentUser ( authentication );
        validateCurrentUserActivation (  curruntUser );
        validateModuleOwnershipForArchive ( module , curruntUser );

        if (!module.getIsActive ( )) {
            throw new IllegalStateException ( "Module is already archived" );
        }

        Long courseId = module.getCourse ( ).getId ( );
        module.setIsActive ( false );
        moduleRepository.save ( module );

        reorderModulesInCourse ( courseId );
    }






    private User getCurrentUser (Authentication authentication) {

      return userRepository.findByEmailIgnoreCase ( authentication.getName ( ) ).orElseThrow (
              () -> new NotFoundException (
                      ErrorCode.USER_NOT_FOUND.toString () ,
                      "User with email " + authentication.getName ( ) + " not found"
              )
      );


    }

    private void validateCurrentUserActivation ( User user) {

      if (! user.isActive ())
          throw new IllegalStateException (
                  "Only active users can manage resources"
          );

    }

    private void validateModuleOwnershipForCreate (ModuleCreationDto dto, User currentUser) {


        if (currentUser.getRole ().equals ( Role.ADMIN ))
            return;

        String currentUserEmail = currentUser.getEmail ( );
        String instructorEmail = moduleRepository.getInstructorEmailByCourseId ( dto.courseId () );

        if ( currentUser.getRole ().equals ( Role.INSTRUCTOR ) && ! currentUserEmail.equals ( instructorEmail )) {
            throw new SecurityException ( "Instructors can only manage their own resources" );
        }
    }

    private void validateModuleOwnershipForUpdate ( Long id , User currentUser) {


        if (currentUser.getRole ().equals ( Role.ADMIN ))
            return;

        String currentUserEmail = currentUser.getEmail ( );
        String instructorEmail = moduleRepository.getInstructorEmailByModuleId ( id );

        if (currentUser.getRole ().equals ( Role.INSTRUCTOR ) &&
                ! instructorEmail.equals ( currentUserEmail ))

            throw new SecurityException ( "Instructors can only manage their own resources" );

    }

    private void validateModuleOwnershipForArchive (Module module , User currentUser) {

        String currentUserEmail = currentUser.getEmail ( );
        String instructorEmail = module.getCourse().getInstructor().getEmail();

       if (currentUser.getRole ().equals ( Role.ADMIN ))
           return;

       if (! currentUserEmail.equals ( instructorEmail ))
           throw new SecurityException ("Instructors can only manage their own resources");

    }


}
