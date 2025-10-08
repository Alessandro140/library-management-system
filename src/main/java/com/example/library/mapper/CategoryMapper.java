package com.example.library.mapper;


import com.example.library.dto.CategoryDTO;
import com.example.library.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @IgnoreAuditFields
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "books", ignore = true)
    Category toEntity(CategoryDTO dto);

    CategoryDTO toDto(Category entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @IgnoreAuditFields
    Category updateCategory(CategoryDTO dto, @MappingTarget Category entity);

}
