package com.example.library.mapper;

import com.example.library.dto.BookDTO;
import com.example.library.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @IgnoreAuditFields
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "loans", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Book toEntity(BookDTO dto);

    BookDTO toDto(Book entity);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "loans", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Book updateBook(BookDTO dto, @MappingTarget Book entity);
}
