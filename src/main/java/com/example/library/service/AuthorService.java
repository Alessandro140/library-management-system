package com.example.library.service;

import com.example.library.dto.AuthorDTO;
import com.example.library.dto.BookDTO;
import com.example.library.entity.Author;
import com.example.library.entity.Book;
import com.example.library.utils.RepositoryException;
import com.example.library.mapper.AuthorMapper;
import com.example.library.mapper.BookMapper;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
import com.example.library.specification.SpecsNotDeleted;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Service implementation of the author entity.
 */
@Service
public class AuthorService {

    private final @NonNull AuthorRepository authorRepository;
    private final @NonNull AuthorMapper authorMapper;
    private final @NonNull BookRepository bookRepository;
    private final @NonNull BookMapper bookMapper;

    public AuthorService(@NonNull AuthorRepository authorRepository, @NonNull AuthorMapper authorMapper, @NonNull BookRepository bookRepository, @NonNull BookMapper bookMapper) {
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
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
     * @throws BookNotFoundException
     * @return the saved author dto
     */
    @Transactional
    public AuthorDTO createAuthor(AuthorDTO authorDTO) throws BookNotFoundException{
        authorDTO.setId(null);

        Author author = authorMapper.toEntity(authorDTO);

        Set<Book> merged = new HashSet<>();
        for (BookDTO bd : authorDTO.getBooks()) {
            Book book;

            book = bookRepository.findById(bd.getId()).orElseThrow(() -> new BookNotFoundException(bd.getId()));
            bookMapper.updateBook(bd, book);
            book.setAuthor(author);
            merged.add(book);
        }
        author.getBooks().clear();
        author.getBooks().addAll(merged);

        Author saved = authorRepository.save(author);
        return authorMapper.toDto(saved);
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
    public @NonNull AuthorDTO updateAuthor(@NonNull Long id, @NonNull AuthorDTO authorDTO) throws AuthorNotFoundException, BookNotFoundException{

        Author authorToModify = this.authorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));

        this.authorMapper.updateAuthor(authorDTO, authorToModify);
        Set<Book> merged = new HashSet<>();
        for (BookDTO bd : authorDTO.getBooks()) {
            Book book;

            book = bookRepository.findById(bd.getId()).orElseThrow(() -> new BookNotFoundException(bd.getId()));
            bookMapper.updateBook(bd, book);
            book.setAuthor(authorToModify);
            merged.add(book);
        }
        authorToModify.getBooks().clear();
        authorToModify.getBooks().addAll(merged);

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
    /**
     * Exception thrown when a book doesn't exists.
     */
    public static class BookNotFoundException extends RepositoryException.NotFound {
        /**
         * Creates a new BookNotFoundException with the given id.
         *
         * @param Isbn - the Isbn of the book
         */
        public BookNotFoundException(@NotNull Long id) {
            super("Book doesn't exist with id: " + id);
        }

    }
}