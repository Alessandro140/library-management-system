package com.example.library.service;

import com.example.library.dto.AuthorDTO;
import com.example.library.dto.BookDTO;
import com.example.library.entity.Author;
import com.example.library.utils.RepositoryException;
import com.example.library.mapper.AuthorMapper;
import com.example.library.repository.AuthorRepository;

import jakarta.validation.constraints.NotNull;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
public class AuthorService {

    private final @NonNull AuthorRepository authorRepository;
    private final @NonNull AuthorMapper authorMapper;

    public AuthorService(@NonNull AuthorRepository authorRepository, @NonNull AuthorMapper authorMapper) {
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
    }

    /**
     * Get a single author by its id.
     *
     * @param id the id of the author
     * @return an optional with the author if found, empty otherwise
     */
    public @NonNull Optional<AuthorDTO> getAuthorById(@NonNull Long id) {
        // Find the book by its ID and map it to a DTO.
        return this.authorRepository.findById(id).map(this.authorMapper::toDto);
    }



}
