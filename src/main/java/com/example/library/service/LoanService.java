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
import com.example.library.service.BookService.BookHasTooManyCopiesException;
import com.example.library.specification.SpecsNotDeleted;
import com.example.library.repository.BookRepository;

import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Map;

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
    public @NonNull Optional<LoanDTO> getLoanById(@NonNull Long id) throws NullInputException{
        if(id == null){
            throw new NullInputException("id");
        }
        return this.loanRepository.findByIdAndDeletedFalse(id)
                .map(this.loanMapper::toDto);
    }


    public @NonNull Page<LoanDTO> getLoans(Specification<Loan> loanSpecification, @NonNull Pageable pageable) throws NullInputException{
        if(pageable == null){
            throw new NullInputException("pageable");
        }
        Specification<Loan> specLoan = SpecsNotDeleted.ensureNotDeleted(loanSpecification);
        return loanRepository.findAll(specLoan, pageable).map(this.loanMapper::toDto);
    }

    /**
     * Create a new loan.
     *
     * @param loan
     * @return the saved loan dto
     */
    @Transactional
    public @NonNull LoanDTO createLoan(@NonNull LoanDTO loanDTO) throws UserNotFoundException, BookNotFoundException, BookHasNotEnoughCopiesException, NullInputException{

        if(loanDTO == null){
            throw new NullInputException("loanDTO");
        }

        loanDTO.setId(null);

        Loan loanToSave = this.loanMapper.toEntity(loanDTO);

        User user = this.userRepository.findByIdAndDeletedFalse(loanDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException(loanDTO.getUserId()));

        loanToSave.setUser(user);
        Set<Long> ids = new HashSet<>();

        for (BookDTO bookDto : loanDTO.getBooks()){
            ids.add(bookDto.getId());
        }

        List<Book> books = bookRepository.findAllByIdInForUpdate(ids);

        List<Long> foundIds = books.stream()
                .map(Book::getId)
                .collect(Collectors.toList());

        List<Long> missingIds = ids.stream()
                .distinct()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());

        if (!missingIds.isEmpty()) {
            throw new BookNotFoundException(missingIds.get(0));
        }

        for(Book book : books){
            if (book.getAvailableCopies() <= 0) {
                throw new BookHasNotEnoughCopiesException(book.getId());
            }
            book.setAvailableCopies(book.getAvailableCopies() - 1);
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
    public LoanDTO updateLoan(Long id, LoanDTO loanDTO) throws LoanNotFoundException,
            UserNotFoundException, BookNotFoundException,
            BookHasNotEnoughCopiesException, BookHasTooManyCopiesException, NullInputException {

        if (id == null && loanDTO == null) throw new NullInputException("id", "loanDTO");
        if (id == null) throw new NullInputException("id");
        if (loanDTO == null) throw new NullInputException("loanDTO");

        Loan loan = loanRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        // ids attuali e desiderati
        Set<Long> newBookIds = loanDTO.getBooks().stream().map(BookDTO::getId).collect(Collectors.toSet());
        Set<Long> oldBookIds = loan.getBooks().stream().map(Book::getId).collect(Collectors.toSet());

        Set<Long> toAdd = new HashSet<>(newBookIds);
        toAdd.removeAll(oldBookIds);

        Set<Long> toRemove = new HashSet<>(oldBookIds);
        toRemove.removeAll(newBookIds);

        if (!toAdd.isEmpty() || !toRemove.isEmpty()) {
            Set<Long> affected = new HashSet<>(toAdd);
            affected.addAll(toRemove);

            List<Book> lockedBooks = bookRepository.findAllByIdInForUpdate(affected);
            if (lockedBooks.size() != affected.size()) {
                Set<Long> found = lockedBooks.stream().map(Book::getId).collect(Collectors.toSet());
                affected.removeAll(found);
                throw new BookNotFoundException(affected.iterator().next());
            }
            Map<Long, Book> bookMap = lockedBooks.stream()
                    .collect(Collectors.toMap(Book::getId, Function.identity()));

            for (Long addId : toAdd) {
                Book b = bookMap.get(addId);
                if (b.getAvailableCopies() <= 0) throw new BookHasNotEnoughCopiesException(addId);
                b.setAvailableCopies(b.getAvailableCopies() - 1);
            }
            for (Long remId : toRemove) {
                Book b = bookMap.get(remId);
                int newAvail = b.getAvailableCopies() + 1;
                if (newAvail > b.getTotalCopies()) throw new BookHasTooManyCopiesException(remId);
                b.setAvailableCopies(newAvail);
            }
            loan.getBooks().removeIf(b -> toRemove.contains(b.getId()));

            for (Long addId : toAdd) {
                Book b = bookMap.get(addId);
                if (b == null) {
                    b = bookRepository.findById(addId).orElseThrow(() -> new BookNotFoundException(addId));
                }
                if (loan.getBooks().stream().noneMatch(x -> x.getId().equals(addId))) {
                    loan.getBooks().add(b);
                }
            }
        }
        Set<Book> snapshot = new HashSet<>(loan.getBooks());

        loanMapper.updateLoan(loanDTO, loan);

        loan.getBooks().clear();
        loan.getBooks().addAll(snapshot);

        User user = userRepository.findByIdAndDeletedFalse(loanDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException(loanDTO.getUserId()));
        loan.setUser(user);

        Loan savedLoan = loanRepository.save(loan);
        loanRepository.flush();

        return loanMapper.toDto(savedLoan);
    }
    /**
     * Update the availables copies of a book, given the book. Used only as utilies to update loan.
     *
     * @param book
     * @param quantity
     * @throws BookHasNotEnoughCopiesException
     * @throws BookHasTooManyCopiesException
     */
    @Transactional
    private void updateBookCopiesWithLock(Book book, Integer quantity)
            throws BookHasNotEnoughCopiesException, BookHasTooManyCopiesException {

        Integer availableCopies = book.getAvailableCopies();

        if(availableCopies + quantity < 0){
            throw new BookHasNotEnoughCopiesException(book.getId());
        } else if(availableCopies + quantity > book.getTotalCopies()){
            throw new BookHasTooManyCopiesException(book.getId());
        }

        book.setAvailableCopies(availableCopies + quantity);
    }

    /**
     * Soft delete a loan and increment available copies.
     *
     * @param id
     * @throws LoanNotFoundException
     */
    @Transactional
    public void softDeleteLoan(@NonNull Long id) throws LoanNotFoundException, NullInputException, BookHasTooManyCopiesException, BookHasNotEnoughCopiesException {
        if(id == null){
            throw new NullInputException("id");
        }

        Loan loan = this.loanRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        if (loan.getBooks() != null && !loan.getBooks().isEmpty()) {
            // Raccogli gli ID dei libri
            Set<Long> bookIds = loan.getBooks().stream()
                    .map(Book::getId)
                    .collect(Collectors.toSet());

            // Lock pessimistico su tutti i libri del prestito
            List<Book> booksToUpdate = bookRepository.findAllByIdInForUpdate(bookIds);

            // Incrementa le copie disponibili per ogni libro
            for(Book book : booksToUpdate){
                updateBookCopiesWithLock(book, 1); // +1 perché restituiamo i libri
            }
        }

        loan.setDeleted(true);
    }

    /**
     * Delete a loan and increment available copies.
     *
     * @param id
     * @throws LoanNotFoundException
     */
    @Transactional
    public void deleteLoan(@NonNull Long id) throws LoanNotFoundException, NullInputException, BookHasTooManyCopiesException, BookHasNotEnoughCopiesException {
        if(id == null){
            throw new NullInputException("id");
        }

        Loan loan = this.loanRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        if (loan.getBooks() != null && !loan.getBooks().isEmpty()) {
            // Raccogli gli ID dei libri
            Set<Long> bookIds = loan.getBooks().stream()
                    .map(Book::getId)
                    .collect(Collectors.toSet());

            // Lock pessimistico su tutti i libri del prestito
            List<Book> booksToUpdate = bookRepository.findAllByIdInForUpdate(bookIds);

            // Incrementa le copie disponibili per ogni libro
            for(Book book : booksToUpdate){
                updateBookCopiesWithLock(book, 1); // +1 perché restituiamo i libri
            }
        }

        this.loanRepository.deleteById(id);
    }

    /**
     * Restore a soft deleted loan and decrement available copies.
     *
     * @param id
     * @return
     * @throws LoanNotFoundException
     */
    @Transactional
    public LoanDTO restoreLoanById(@NonNull Long id) throws LoanNotFoundException, NullInputException, BookHasNotEnoughCopiesException, BookHasTooManyCopiesException {
        if(id == null){
            throw new NullInputException("id");
        }

        Loan loanToRestore = this.loanRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        if (loanToRestore.getBooks() != null && !loanToRestore.getBooks().isEmpty()) {
            // Raccogli gli ID dei libri
            Set<Long> bookIds = loanToRestore.getBooks().stream()
                    .map(Book::getId)
                    .collect(Collectors.toSet());

            // Lock pessimistico su tutti i libri del prestito
            List<Book> booksToUpdate = bookRepository.findAllByIdInForUpdate(bookIds);

            // Decrementa le copie disponibili per ogni libro
            for(Book book : booksToUpdate){
                updateBookCopiesWithLock(book, -1); // -1 perché riprendiamo i libri in prestito
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
     * Exception thrown when a book has not enough copies available.
     */
    public static class BookHasNotEnoughCopiesException extends RepositoryException.Conflict {
        /**
         * Creates a new BookHasNotEnoughCopiesException with the given id.
         *
         * @param id - the Isbn of the book
         */
        public BookHasNotEnoughCopiesException(@NotNull Long id) {
            super("The book with this id doesn't have enough copies: " + id);
        }
    }


    /**
     * Exception thrown when a book has too many copies available.
     */
    public static class BookHasTooManyCopiesException extends RepositoryException.Conflict {
        /**
         * Creates a new BookHasTooManyCopiesException with the given id.
         *
         * @param id - the Isbn of the book
         */
        public BookHasTooManyCopiesException(@NotNull Long id) {
            super("The book with this id has too many copies: " + id);
        }
    }

    public static class NullInputException extends RepositoryException.BadRequest{

        public NullInputException(@NotNull String parameterName){
            super("This parameter should be NonNull: " + parameterName);
        }

        public NullInputException(@NotNull String parameterName1, @NotNull String parameterName2){
            super("This parameters should be NonNull: " + parameterName1 + ", " + parameterName2);
        }
    }
}
