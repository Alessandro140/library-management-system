package com.example.library.service;

import com.example.library.dto.BookDTO;
import com.example.library.dto.LoanDTO;
import com.example.library.entity.Loan;
import com.example.library.entity.User;
import com.example.library.entity.Book;
import com.example.library.utils.RepositoryException;
import com.example.library.mapper.LoanMapper;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.LoanService.LoanNotFoundException;
import com.example.library.repository.BookRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LoanService {

    private final @NonNull LoanRepository loanRepository;
    private final @NonNull UserRepository userRepository;
    private final @NonNull BookRepository bookRepository;
    private final @NonNull LoanMapper loanMapper;

    public LoanService(@NonNull LoanRepository loanRepository, @NonNull LoanMapper loanMapper,
                         @NonNull UserRepository userRepository, @NonNull BookRepository bookRepository){
        this.loanMapper = loanMapper;
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * Return an optional with inside a loanDTO or empty.
     *
     * @param id
     * @param pageable
     * @return an optional with the dto of the loan
     */
    public @NonNull Optional<LoanDTO> getLoanById(@NonNull Long id){
        return this.loanRepository.findByIdAndDeletedFalse(id)
                .map(this.loanMapper::toDto);
    }


    public @NonNull Page<LoanDTO> getLoans(Specification<Loan> loanSpecification, @NonNull Pageable pageable){
        Specification<Loan> notDeletedSpec = (root, query, criteriaBuilder) ->
            criteriaBuilder.isFalse(root.get("deleted"));

        if (loanSpecification != null) {
            loanSpecification = loanSpecification.and(notDeletedSpec);
        } else {
            loanSpecification = notDeletedSpec;
        }

        return loanRepository.findAll(loanSpecification, pageable).map(this.loanMapper::toDto);

    }

    /**
     * Create a new loan.
     *
     * @param loan
     * @return the saved loan dto
     */
    @Transactional
    public @NonNull LoanDTO createLoan(@NonNull LoanDTO loanDTO) throws UserNotFoundException, BookNotFoundException, BookHasNotEnoughCopiesException{

        loanDTO.setId(null);

        Loan loanToSave = this.loanMapper.toEntity(loanDTO);

        User user = this.userRepository.findByIdAndDeletedFalse(loanDTO.getUser_id())
                .orElseThrow(() -> new UserNotFoundException(loanDTO.getUser_id()));

        loanToSave.setUser(user);
        List<Long> ids = new ArrayList<>();
        for (BookDTO bookDto : loanDTO.getBooks()){
            ids.add(bookDto.getId());
        }

        List<Book> books = bookRepository.findAllByIdInForUpdate(ids);

        Set<Long> foundIds = books.stream()
                .map(Book::getId)
                .collect(Collectors.toSet());

        List<Long> missingIds = ids.stream()
                .distinct()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());

        if (!missingIds.isEmpty()) {
            throw new BookNotFoundException(missingIds.get(0));
        }

        for(Book book : books){
            if (book.getAvailable_copies() <= 0) {
                throw new BookHasNotEnoughCopiesException(book.getId());
            }
            book.setAvailable_copies(book.getAvailable_copies() - 1);
        }
        Set<Book> loanBooks = new HashSet<>(books);
        loanToSave.getBooks().clear();
        loanToSave.getBooks().addAll(loanBooks);

        Loan savedLoan = this.loanRepository.save(loanToSave);

        return this.loanMapper.toDto(savedLoan);
    }

    /* update this to do the check on the books: if the book has changed or the status has changed. If the
     * status get to returned update the available copies. May need to verify if the copies of the book exist
     * if they are different from before change the available copies.
     */
    /**
     * Update a loan.
     *
     * @param id
     * @param loanDTO
     * @return the DTO of the modified loan
     * @throws LoanNotFoundException
     */
    @Transactional
    public @NonNull LoanDTO updateLoan(@NonNull Long id, @NonNull LoanDTO loanDTO) throws LoanNotFoundException, UserNotFoundException{

        Loan loanToModify = this.loanRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        this.loanMapper.updateLoan(loanDTO, loanToModify);

        User user = this.userRepository.findByIdAndDeletedFalse(loanDTO.getUser_id())
                .orElseThrow(() -> new UserNotFoundException(loanDTO.getUser_id()));
        loanToModify.setUser(user);

        return this.loanMapper.toDto(loanToModify);
    }

    /*Modify this to increment the available copies of the deleted loan */
    /**
     * Soft delete an loan.
     *
     * @param id
     * @throws LoanNotFoundException
     */
    @Transactional
    public void softDeleteLoan(@NonNull Long id) throws LoanNotFoundException{
        Loan loan = this.loanRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        if (loan.getBooks() != null || !loan.getBooks().isEmpty()) {
            for(Book book : loan.getBooks()){
                book.setAvailable_copies(book.getAvailable_copies() - 1);
                bookRepository.save(book);
            }
        }
        loan.setDeleted(true);
    }

    /*Modify this to increment the available copies of the deleted loan */
    /**
     * Delete an loan
     *
     * @param id
     * @throws LoanNotFoundException
     */
    @Transactional
    public void deleteLoan(@NonNull Long id) throws LoanNotFoundException{
        Loan loan = this.loanRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        if (loan.getBooks() != null || !loan.getBooks().isEmpty()) {
            for(Book book : loan.getBooks()){
                book.setAvailable_copies(book.getAvailable_copies() - 1);
                bookRepository.save(book);
            }
        }
        this.loanRepository.deleteById(id);
    }


    /**
     * Restore a soft deleted loan by its id.
     *
     * @param id
     * @return
     * @throws LoanNotFoundException
     */
    @Transactional
    public LoanDTO restoreLoanById(@NonNull Long id) throws LoanNotFoundException{
        Loan loanToRestore = this.loanRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        if (loanToRestore.getBooks() != null || !loanToRestore.getBooks().isEmpty()) {
            for(Book book : loanToRestore.getBooks()){
                book.setAvailable_copies(book.getAvailable_copies() - 1);
                bookRepository.save(book);
            }
        }
        loanToRestore.setDeleted(false);

        return this.loanMapper.toDto(loanToRestore);
    }

    /**
     * Exception thrown when a loan doesn't exist.
     */
    public static class LoanNotFoundException extends RepositoryException.NotFound {
        /**
         * Creates a new LoanNotFoundException with the given id.
         *
         * @param id - the id of the loan
         */
        public LoanNotFoundException(@NotNull Long id) {
            super("Loan doesn't exist with id: " + id);
        }
    }

    /**
     * Exception thrown when a user doesn't exists.
     */
    public static class UserNotFoundException extends RepositoryException.NotFound {
        /**
         * Creates a new UserNotFoundException with the given id.
         *
         * @param id - the id of the user
         */
        public UserNotFoundException(@NotNull Long id) {
            super("User doesn't exist with id: " + id);
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

    /**
     * Exception thrown when a book already exists.
     */
    public static class BookHasNotEnoughCopiesException extends RepositoryException.Conflict {
        /**
         * Creates a new BookAlreadyExistsException with the given Isbn.
         *
         * @param id - the Isbn of the book
         */
        public BookHasNotEnoughCopiesException(@NotNull Long id) {
            super("The book with this id doesn't have enough copies: " + id);
        }
    }
}
