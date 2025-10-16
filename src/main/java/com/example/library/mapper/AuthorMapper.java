package com.example.library.mapper;

import com.example.library.dto.AuthorDTO;
import com.example.library.entity.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring", uses = BookMapper.class)
public interface AuthorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "books", ignore = true)
    @IgnoreAuditFields
    Author updateAuthor(AuthorDTO dto, @MappingTarget Author entity);

    AuthorDTO toDto(Author entity);

    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "books", ignore = true)
    @IgnoreAuditFields
    Author toEntity(AuthorDTO dto);
}
