package com.example.library.mapper;

import com.example.library.dto.AuthorDTO;
import com.example.library.entity.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {BookMapper.class})
public interface AuthorMapper {

    @IgnoreAuditFields
    @Mapping(target = "isDeleted", ignore = true)
    Author toEntity(AuthorDTO dto);

    AuthorDTO toDto(Author entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @IgnoreAuditFields
    Author updateAuthor(AuthorDTO dto, @MappingTarget Author entity);
}
