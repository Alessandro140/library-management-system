package com.example.library.service;

import com.example.library.dto.BookDTO;
import com.example.library.entity.Book;
import com.example.library.entity.Author;
import com.example.library.utils.RepositoryException;
import com.example.library.mapper.BookMapper;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service implementation for the Book entity.
 */
@Service
public class BookService {

    private final @NonNull BookRepository bookRepository;
    private final @NonNull BookMapper bookMapper;
    private final @NonNull AuthorRepository authorRepository;

    public BookService(@NonNull BookRepository bookRepository, @NonNull BookMapper bookMapper, @NonNull AuthorRepository authorRepository){
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.authorRepository = authorRepository;

    }

    /**
     * Get a single book by its id.
     *
     * @param id the id of the book
     * @return an optional with the book if found, empty otherwise
     */
    public @NonNull Optional<BookDTO> getBookById(@NonNull Long id){

        return this.bookRepository.findByIdAndDeletedFalse(id).map(this.bookMapper::toDto);
    }

     /**
     * Get a single book by its ISBN.
     *
     * @param ISBN the id of the book
     * @return an optional with the book if found, empty otherwise
     */
    public @NonNull Optional<BookDTO> getBookByISBN(@NonNull String ISBN){

        return this.bookRepository.findByISBNAndDeletedFalse(ISBN).map(this.bookMapper::toDto);
    }

    public Page<BookDTO> getBooks(@Nullable Specification<Book> bookSpecification, @NonNull Pageable pageable){

        Specification<Book> notDeletedSpec = (root, query, criteriaBuilder) ->
            criteriaBuilder.isFalse(root.get("deleted"));

        if (bookSpecification != null) {
            bookSpecification = bookSpecification.and(notDeletedSpec);
        } else {
            bookSpecification = notDeletedSpec;
        }

        return this.bookRepository.findAll(bookSpecification, pageable).map(this.bookMapper::toDto);
    }


    /**
     * Create a new book.
     *
     * @param bookDTO the book to create
     * @return the created book
     * @throws BookAlreadyExistsException if the book already exist
     */
    @Transactional
    public @NonNull BookDTO createBook(@NonNull BookDTO bookDTO) throws BookAlreadyExistsException, AuthorNotFoundException{
        if(this.bookRepository.findByISBNAndDeletedFalse(bookDTO.getISBN()).isPresent()){
            throw new BookAlreadyExistsException(bookDTO.getISBN());
        }

        bookDTO.setId(null);
        Book book = this.bookMapper.toEntity(bookDTO);
        Author author = this.authorRepository.findByIdAndDeletedFalse(bookDTO.getAuthorId())
                .orElseThrow(() -> new AuthorNotFoundException(bookDTO.getAuthorId()));
        book.setAuthor(author);
        Book savedBook = this.bookRepository.save(book);

        return this.bookMapper.toDto(savedBook);
    }

    /**
     * Modify a book
     *
     * @param id
     * @param bookDTO
     * @return the updated book
     * @throws BookNotFoundException
     */
    @Transactional
    public @NonNull BookDTO updateBook(@NonNull Long id, @NonNull BookDTO bookDTO) throws BookNotFoundException, AuthorNotFoundException {
        Book book = this.bookRepository.findByIdAndDeletedFalse(id).
                orElseThrow(() -> new BookNotFoundException(id));

        Author author = this.authorRepository.findByIdAndDeletedFalse(bookDTO.getAuthorId()).orElseThrow(() -> new AuthorNotFoundException(bookDTO.getAuthorId()));
        this.bookMapper.updateBook(bookDTO, book);
        book.setAuthor(author);
        return this.bookMapper.toDto(book);
    }

    /**
     * Soft delete a book.
     *
     * @param id
     * @throws BookNotFoundException
     */
    @Transactional
    public void softDeleteBook(@NonNull Long id) throws BookNotFoundException{
        Book book = this.bookRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        book.setDeleted(true);
    }

    /**
     * Delete a book.
     *
     * @param id
     * @throws BookNotFoundException
     */
    @Transactional
    public void deleteBook(@NonNull Long id) throws BookNotFoundException {
        if (!this.bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        this.bookRepository.deleteById(id);
    }

    /**
     * Update the number of available copies of a book + quantities to add
     * - quantities to reduce.
     *
     * @param id
     * @param quantities
     * @return
     * @throws BookNotFoundException
     * @throws BookHasNotEnoughCopiesException
     * @throws BookHasTooManyCopiesException
     */
    @Transactional
    public BookDTO updateAvailableCopies(@NonNull Long id, @NonNull Integer quantities) throws BookNotFoundException, BookHasNotEnoughCopiesException, BookHasTooManyCopiesException {

        Book bookToUpdate = this.bookRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        Integer availableCopies = bookToUpdate.getAvailable_copies();
        if(availableCopies + quantities < 0){
            throw new BookHasNotEnoughCopiesException(id);
        } else if(availableCopies + quantities > bookToUpdate.getTotal_copies()){
            throw new BookHasTooManyCopiesException(id);
        } else{
            bookToUpdate.setAvailable_copies(availableCopies + quantities);
        }
        return this.bookMapper.toDto(bookToUpdate);
    }

    /**
     * Update the total number of copies of a book + quantities to add
     * - quantities to reduce.
     *
     * @param id
     * @param quantities
     * @return
     * @throws BookNotFoundException
     * @throws BookHasNotEnoughCopiesException
     */
    @Transactional
    public BookDTO updateTotalCopies(@NonNull Long id, Integer quantities)throws BookNotFoundException, BookHasNotEnoughCopiesException{
        Book bookToUpdate = this.bookRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        Integer totalCopies = bookToUpdate.getTotal_copies();

        if(totalCopies + quantities < 0){
            throw new BookHasNotEnoughCopiesException(id);
        }

        bookToUpdate.setTotal_copies(totalCopies + quantities);

        return this.bookMapper.toDto(bookToUpdate);
    }

    /**
     * Restore a soft deleted book by its id.
     *
     * @param id
     * @return
     * @throws BookNotFoundException
     */
    @Transactional
    public BookDTO restoreBookById(@NonNull Long id) throws BookNotFoundException{
        Book bookToRestore = this.bookRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        bookToRestore.setDeleted(false);

        return this.bookMapper.toDto(bookToRestore);
    }

    /**
     * Restore a soft deleted book by its ISBN.
     *
     * @param ISBN
     * @return
     * @throws BookNotFoundException
     */
    @Transactional
    public BookDTO restoreBookByISBN(@NonNull String ISBN) throws BookNotFoundException{
        Book bookToRestore = this.bookRepository.findByISBNAndDeletedTrue(ISBN)
                .orElseThrow(() -> new BookNotFoundException(ISBN));

        bookToRestore.setDeleted(false);

        return this.bookMapper.toDto(bookToRestore);
    }

    /**
     * Exception thrown when a book already exists.
     */
    public static class BookAlreadyExistsException extends RepositoryException.Conflict {
        /**
         * Creates a new BookAlreadyExistsException with the given ISBN.
         *
         * @param isbn - the ISBN of the book
         */
        public BookAlreadyExistsException(@NotNull String isbn) {
            super("Book already exists with ISBN: " + isbn);
        }
    }

    /**
     * Exception thrown when a book already exists.
     */
    public static class BookHasNotEnoughCopiesException extends RepositoryException.Conflict {
        /**
         * Creates a new BookAlreadyExistsException with the given ISBN.
         *
         * @param id - the ISBN of the book
         */
        public BookHasNotEnoughCopiesException(@NotNull Long id) {
            super("The book with this id doesn't have enough copies: " + id);
        }
    }

    /**
     * Exception thrown when a book has too many available copies.
     */
    public static class BookHasTooManyCopiesException extends RepositoryException.Conflict {
        /**
         * Creates a new BookAlreadyExistsException with the given ISBN.
         *
         * @param id - the ISBN of the book
         */
        public BookHasTooManyCopiesException(@NotNull Long id) {
            super("The book with this id has more available copies then total: " + id);
        }
    }
    /**
     * Exception thrown when a book doesn't exists.
     */
    public static class BookNotFoundException extends RepositoryException.NotFound {
        /**
         * Creates a new BookNotFoundException with the given id.
         *
         * @param isbn - the ISBN of the book
         */
        public BookNotFoundException(@NotNull Long id) {
            super("Book doesn't exist with id: " + id);
        }

        /**
         * Creates a new BookNotFoundException with the given ISBN.
         *
         * @param isbn - the ISBN of the book
         */
        public BookNotFoundException(@NotNull String ISBN) {
            super("Book doesn't exist with id: " + ISBN);
        }

    }

    /**
     * Exception thrown when an Author doesn't exists.
     */
    public static class AuthorNotFoundException extends RepositoryException.NotFound {
        /**
         * Creates a new AuthorNotFoundException with the given id.
         *
         * @param id - the id of the author
         */
        public AuthorNotFoundException(@NotNull Long id) {
            super("Author doesn't exist with id: " + id);
        }
    }

}
