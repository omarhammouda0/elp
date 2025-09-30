package com.example.demo.mapper;

import com.example.demo.entity.module.Module;
import com.example.demo.entity.module.ModuleCreationDto;
import com.example.demo.entity.module.ModuleResponseDto;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ModuleMapper {

    public ModuleResponseDto toModuleResponseDto(Module m) {

        Objects.requireNonNull(m, "Module cannot be null");

        return new ModuleResponseDto(

                m.getId (),
                m.getTitle (),
                m.getDescription (),
                m.getOrderIndex (),
                m.getIsActive (),
                m.getCourse ().getTitle (),
                m.getCreatedAt ()
        );

    }

    public Module toModule(ModuleCreationDto dto) {

        Objects.requireNonNull(dto, "Module cannot be null");
        Objects.requireNonNull ( dto.title (), "Title cannot be null");

        if (dto.orderIndex () != null && dto.orderIndex () <= 0 ) {
            throw new IllegalArgumentException ( "Order index cannot be less than zero");
        }

        String trimmedTitle = dto.title ().replaceAll("\\s+", " ").trim ();

        if (trimmedTitle.isEmpty ())
            throw new IllegalArgumentException ("Title cannot be empty");


        String trimmedDescription = dto.description() != null ?
                dto.description().replaceAll("\\s+", " ").trim() : null;

        return Module.builder ( )

                .title (  trimmedTitle )
                .description(trimmedDescription != null && trimmedDescription.isEmpty() ? null : trimmedDescription)
                .orderIndex ( dto.orderIndex ( ) )
                .isActive(dto.isActive() != null ? dto.isActive() : true)
                .build ( );

    };

}
