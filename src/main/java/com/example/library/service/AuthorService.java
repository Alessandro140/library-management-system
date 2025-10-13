package com.example.library.service;

import com.example.library.dto.AuthorDTO;
import com.example.library.entity.Author;
import com.example.library.utils.RepositoryException;
import com.example.library.mapper.AuthorMapper;
import com.example.library.repository.AuthorRepository;
import com.example.library.specification.SpecsNotDeleted;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

/**
 * Service implementation of the author entity.
 */
@Service
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
        // Find the author by its ID and map it to a DTO.
        return this.authorRepository.findByIdAndDeletedFalse(id).map(this.authorMapper::toDto);
    }

    /**
     * Get all the authors.
     *
     * @param authorSpecification
     * @param pageable
     * @return A page with all the authors
     */
    public @NonNull Page<AuthorDTO> getAuthors(@Nullable Specification<Author> authorSpecification, @NonNull Pageable pageable){

        Specification<Author> specAuthor = SpecsNotDeleted.ensureNotDeleted(authorSpecification);

        return authorRepository.findAll(specAuthor, pageable).map(this.authorMapper::toDto);
    }

    /**
     * Create a new author.
     *
     * @param author
     * @return the saved author dto
     */
    @Transactional
    public @NonNull AuthorDTO createAuthor(@NonNull AuthorDTO authorDTO){

        authorDTO.setId(null);

        Author authorToSave = this.authorMapper.toEntity(authorDTO);
        Author savedAuthor = this.authorRepository.save(authorToSave);

        return this.authorMapper.toDto(savedAuthor);
    }

    /**
     * Update and author.
     *
     * @param id
     * @param authorDTO
     * @return the DTO of the modified author
     * @throws AuthorNotFoundException
     */
    @Transactional
    public @NonNull AuthorDTO updateAuthor(@NonNull Long id, @NonNull AuthorDTO authorDTO) throws AuthorNotFoundException{

        Author authorToModify = this.authorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));

        this.authorMapper.updateAuthor(authorDTO, authorToModify);

        return this.authorMapper.toDto(authorToModify);
    }

    /**
     * Soft delete an author.
     *
     * @param id
     * @throws AuthorNotFoundException
     */
    @Transactional
    public void softDeleteAuthor(@NonNull Long id) throws AuthorNotFoundException{
        Author author = this.authorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));
        author.setDeleted(true);
    }

    /**
     * Delete an author
     *
     * @param id
     * @throws AuthorNotFoundException
     */
    @Transactional
    public void deleteAuthor(@NonNull Long id) throws AuthorNotFoundException{
        if (!this.authorRepository.existsById(id)) {
            throw new AuthorNotFoundException(id);
        }
        this.authorRepository.deleteById(id);
    }


    /**
     * Restore a soft deleted author by its id.
     *
     * @param id
     * @return
     * @throws AuthorNotFoundException
     */
    @Transactional
    public AuthorDTO restoreAuthorById(@NonNull Long id) throws AuthorNotFoundException{
        Author authorToRestore = this.authorRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));

        authorToRestore.setDeleted(false);

        return this.authorMapper.toDto(authorToRestore);
    }

    /**
     * Exception thrown when a author already exists.
     */
    public static class AuthorNotFoundException extends RepositoryException.NotFound {
        /**
         * Creates a new AuthorAlreadyExistsException with the given id.
         *
         * @param id - the id of the author
         */
        public AuthorNotFoundException(@NotNull Long id) {
            super("Author doesn't exist with id: " + id);
        }
    }

}
