package com.example.demo.module;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/modules")

public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    //     ________________________Create__________________________

    @PostMapping
    public ResponseEntity <ModuleResponseDto> createModule (@Valid @RequestBody ModuleCreationDto dto) {
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( moduleService.createModule (dto) );
    }


    //     ________________________Read__________________________

    @GetMapping
    public ResponseEntity<Page<ModuleResponseDto>> getAllModules(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(moduleService.getAllModulesOrdered (pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleResponseDto> getModuleById(@PathVariable Long id) {
        return ResponseEntity.ok ( moduleService.getModuleById ( id ) );
    }


    @GetMapping("/course_id/{id}")
    public ResponseEntity<Page<ModuleResponseDto>> getModulesByCourseId (@PathVariable Long id , Pageable pageable) {
        return ResponseEntity.ok ( moduleService.getModulesByCourseId ( id , pageable  ) );
    }

    //     ________________________Update__________________________

    @PutMapping ("/{id}")
    public ResponseEntity <ModuleResponseDto> updateCourse( @PathVariable Long id ,
                                                            @Valid @RequestBody ModuleUpdateDto dto) {

        return ResponseEntity.ok ( moduleService.updateModule ( id , dto ) );
    }

    //     ________________________Delete__________________________

    @DeleteMapping("/{id}")
    public ResponseEntity <Void> archiveModule (@PathVariable Long id) {
        moduleService.archiveModule (id);
        return ResponseEntity.noContent().build();
    }

}
