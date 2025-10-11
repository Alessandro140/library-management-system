package com.example.library.mapper;

import com.example.library.dto.BookDTO;
import com.example.library.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookMapper {

    // Map from DTO to Entity. We keep relations ignored here because assembling
    // Author/Categories/Loans usually requires repositories/services.
    @IgnoreAuditFields
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "loans", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Book toEntity(BookDTO dto);

    // Map from Entity to DTO. Map nested author.id to authorId explicitly.
    @Mapping(target = "author_id", source = "author.id")
    BookDTO toDto(Book entity);

    // Update existing entity from DTO: ignore id and relations and audit fields.
    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "loans", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Book updateBook(BookDTO dto, @MappingTarget Book entity);
}