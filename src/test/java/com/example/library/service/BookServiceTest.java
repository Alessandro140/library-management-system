package com.example.library.service;

import com.example.library.entity.Author;
import com.example.library.entity.Category;
import com.example.library.entity.Loan;
import com.example.library.dto.BookDTO;
import com.example.library.entity.Book;
import com.example.library.mapper.BookMapper;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
import com.example.library.service.BookService.BookAlreadyExistsException;
import com.example.library.service.BookService.BookHasNotEnoughCopiesException;
import com.example.library.service.BookService.BookHasTooManyCopiesException;
import com.example.library.service.BookService.BookNotFoundException;
import com.example.library.service.BookService.NullInputException;
import com.example.library.service.BookService.AuthorNotFoundException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Tests")
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorRepository authorRepository;
    private final BookMapper bookMapper = Mappers.getMapper(BookMapper.class);

    private BookService bookService;

    private Author testAuthor;
    private Author testAuthor2;
    private Book testBook;
    private Book testBook2;
    private BookDTO testBookDTO;
    private BookDTO testBookDTO2;
    private Category category1, category2;
    private Loan loan1;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, bookMapper, authorRepository);

        // Author
        testAuthor = new Author();
        testAuthor2 = new Author();

        testAuthor.setId(1L);
        testAuthor.setFirstName("J.R.R.");
        testAuthor.setLastName("Tolkien");
        testAuthor.setBirthDate(LocalDate.of(1892, 1, 3));

        testAuthor2.setId(2L);
        testAuthor2.setFirstName("J.K.");
        testAuthor2.setLastName("Rowling");
        testAuthor2.setBirthDate(LocalDate.of(1965, 7, 31));

        // Categories
        category1 = new Category();
        category1.setName("Fantasy");

        category2 = new Category();
        category2.setName("Science");

        // Loan
        loan1 = new Loan();
        loan1.setLoanDate(LocalDate.now());

        // Book 1
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("LOTR");
        testBook.setDescription("A book used for test");
        testBook.setIsbn("1234567890123");
        testBook.setAuthor(testAuthor);
        testBook.setTotalCopies(10);
        testBook.setAvailableCopies(5);
        testBook.setPublishedDate(LocalDate.of(2020, 1, 1));
        testBook.getCategories().add(category1);
        testBook.getCategories().add(category2);
        testBook.getLoans().add(loan1);

        // Book 2
        testBook2 = new Book();
        testBook2.setId(2L);
        testBook2.setTitle("The Hobbit");
        testBook2.setDescription("A book used for test");
        testBook2.setIsbn("1234567890125");
        testBook2.setAuthor(testAuthor);
        testBook2.setTotalCopies(8);
        testBook2.setAvailableCopies(6);
        testBook2.setPublishedDate(LocalDate.of(2020, 1, 1));
        testBook2.getCategories().add(category1);
        testBook2.getCategories().add(category2);
        testBook2.getLoans().add(loan1);

        // DTOs
        testBookDTO = new BookDTO(
            testBook.getId(), testBook.getTitle(), testBook.getDescription(),
            testBook.getAvailableCopies(), testBook.getTotalCopies(),
            testBook.getIsbn(), testBook.getAuthor().getId(), testBook.getPublishedDate()
        );

        testBookDTO2 = new BookDTO(
            testBook2.getId(), testBook2.getTitle(), testBook2.getDescription(),
            testBook2.getAvailableCopies(), testBook2.getTotalCopies(),
            testBook2.getIsbn(), testBook2.getAuthor().getId(), testBook2.getPublishedDate()
        );
    }

    @AfterEach
    void tearDown() {
        testAuthor = null;
        testAuthor2 = null;
        category1 = null;
        category2 = null;
        loan1 = null;
        testBook = null;
        testBook2 = null;
        testBookDTO = null;
        testBookDTO2 = null;
    }

    @Nested
    @DisplayName("getBookById")
    class GetBookById {

        @Test
        @DisplayName("should return a book when found")
        void shouldReturnBookWhenFound() throws BookService.NullInputException{
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

            Optional<BookDTO> result = bookService.getBookById(1L);

            assertThat(result).isPresent();
            assertThat(result.get()).usingRecursiveComparison().isEqualTo(testBookDTO);
            verify(bookRepository).findById(1L);
        }

        @Test
        @DisplayName("should return empty when book not found")
        void shouldNotReturnBookWhenFound() throws BookService.NullInputException {
            when(bookRepository.findById(2L)).thenReturn(Optional.empty());

            Optional<BookDTO> result = bookService.getBookById(2L);

            assertThat(result).isEmpty();
            verify(bookRepository).findById(2L);
        }

        @Test
        @DisplayName("should throw an exception if the input is null")
        void getBookById_shouldThrowNullPointerException_whenIdIsNull() throws BookService.NullInputException {
            assertThatThrownBy(() -> bookService.getBookById(null))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameter should be NonNull: id");
        }
    }

    @Nested
    @DisplayName("getBooks")
    class GetBooks {

        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenPageIsNull() throws BookService.NullInputException {
            assertThatThrownBy(() -> bookService.getBooks(null, null))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameter should be NonNull: pageable");
        }

        @Test
        @DisplayName("should return paged books")
        void shouldReturnPagedBooks() throws NullInputException {
            Pageable pageable = PageRequest.of(0, 10);
            Specification<Book> spec = Specification.where(null);
            List<Book> books = Arrays.asList(testBook, testBook2);
            Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

            when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(bookPage);

            Page<BookDTO> result = bookService.getBooks(spec, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0)).usingRecursiveComparison().isEqualTo(testBookDTO);
            assertThat(result.getContent().get(1)).usingRecursiveComparison().isEqualTo(testBookDTO2);

            verify(bookRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("createBook")
    class CreateBook{

        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenbookDTOIsNull() throws BookAlreadyExistsException, AuthorNotFoundException, NullInputException {
            assertThatThrownBy(() -> bookService.createBook(null))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameter should be NonNull: bookDTO");
        }

        @Test
        @DisplayName("should create book successfully")
        void shouldCreateBookSuccessfully() throws BookAlreadyExistsException, AuthorNotFoundException, NullInputException{
        	when(bookRepository.findByIsbn(testBookDTO2.getIsbn())).thenReturn(Optional.empty());
            when(bookRepository.save(any(Book.class))).thenReturn(testBook2);
            when(authorRepository.findById(testBookDTO2.getAuthorId())).thenReturn(Optional.of(testAuthor));
            BookDTO result = bookService.createBook(testBookDTO2);
            testBookDTO2.setId(2L);
            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(testBookDTO2);
			verify(bookRepository).save(any(Book.class));
			verify(bookRepository).findByIsbn(testBookDTO2.getIsbn());
			verify(authorRepository).findById(testBookDTO2.getAuthorId());

            verifyNoMoreInteractions(bookRepository);
            verifyNoMoreInteractions(authorRepository);
        }

        @Test
        @DisplayName("should not create book successfully")
        void shouldNotCreateBookSuccessfully() throws BookAlreadyExistsException, AuthorNotFoundException{
            when(bookRepository.findByIsbn(testBookDTO2.getIsbn())).thenReturn(Optional.of(testBook2));

            assertThatThrownBy(() -> bookService.createBook(testBookDTO2))
                .isInstanceOf(BookAlreadyExistsException.class)
                .hasMessageContaining("Book already exists with Isbn: " + testBookDTO2.getIsbn());

            verify(bookRepository).findByIsbn(testBookDTO2.getIsbn());
            verifyNoMoreInteractions(bookRepository);
        }

        @Test
        @DisplayName("should not create book successfully, author not found")
        void shouldNotCreateBookSuccessfullyNoAuthor() throws BookAlreadyExistsException, AuthorNotFoundException{
            when(bookRepository.findByIsbn(testBookDTO2.getIsbn())).thenReturn(Optional.empty());
            when(authorRepository.findById(testBookDTO2.getAuthorId())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> bookService.createBook(testBookDTO2))
                .isInstanceOf(AuthorNotFoundException.class)
                .hasMessageContaining("Author doesn't exist with id: " + testBookDTO2.getAuthorId());

            verify(bookRepository).findByIsbn(testBookDTO2.getIsbn());
            verifyNoMoreInteractions(bookRepository);
            verify(authorRepository).findById(testBookDTO2.getAuthorId());
            verifyNoMoreInteractions(authorRepository);
        }

    }

    @Nested
    @DisplayName("updateBook")
    class UpdateBook{

        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenIdAndBookDTOAreNull() throws BookNotFoundException, AuthorNotFoundException, BookAlreadyExistsException, NullInputException {
            assertThatThrownBy(() -> bookService.updateBook(null, null))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameters should be NonNull: id, bookDTO");
        }


        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenBookDTOIsNull() throws BookNotFoundException, AuthorNotFoundException, BookAlreadyExistsException, NullInputException {
            assertThatThrownBy(() -> bookService.updateBook(1L, null))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameter should be NonNull: bookDTO");
        }


        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenIdIsNull() throws BookNotFoundException, AuthorNotFoundException, BookAlreadyExistsException, NullInputException {
            assertThatThrownBy(() -> bookService.updateBook(null, testBookDTO))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameter should be NonNull: id");
        }


        @Test
        @DisplayName("should update a book")
        void shouldUpdateBookSuccessfully() throws BookNotFoundException, AuthorNotFoundException, BookAlreadyExistsException, NullInputException {
            when(bookRepository.findById(testBook.getId())).thenReturn(Optional.of(testBook));
            when(authorRepository.findById(testBookDTO2.getAuthorId())).thenReturn(Optional.of(testAuthor));
            when(bookRepository.findByIsbn(testBookDTO2.getIsbn())).thenReturn(Optional.empty());

            BookDTO result = bookService.updateBook(testBook.getId(), testBookDTO2);

            testBookDTO2.setId(testBook.getId());

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(testBookDTO2);

            verify(bookRepository).findById(testBook.getId());
            verify(bookRepository).findByIsbn(testBookDTO2.getIsbn());
            verifyNoMoreInteractions(bookRepository);
            verify(authorRepository).findById(testBookDTO2.getAuthorId());
            verifyNoMoreInteractions(authorRepository);
        }

        @Test
        @DisplayName("should not update the book")
        void shouldNotUpdateBookSuccessfully() throws BookNotFoundException, AuthorNotFoundException, BookAlreadyExistsException {
            when(bookRepository.findById(testBook.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.updateBook(testBook.getId(),testBookDTO2))
                .isInstanceOf(BookService.BookNotFoundException.class)
                .hasMessageContaining("Book doesn't exist with id: " + testBook.getId());

        }

        @Test
        @DisplayName("should not update a book")
        void shouldNotUpdateBookSuccessfullyAuthorNotFound() throws BookNotFoundException, AuthorNotFoundException, BookAlreadyExistsException {
            when(bookRepository.findById(testBook.getId())).thenReturn((Optional.of(testBook)));
            when(authorRepository.findById(testBookDTO2.getAuthorId())).thenReturn(Optional.empty());
            when(bookRepository.findByIsbn(testBookDTO2.getIsbn())).thenReturn(Optional.empty());

            testBookDTO2.setId(testBook.getId());
            assertThatThrownBy(() -> bookService.updateBook(testBook.getId(),testBookDTO2))
                .isInstanceOf(BookService.AuthorNotFoundException.class)
                .hasMessageContaining("Author doesn't exist with id: " + testBookDTO2.getAuthorId());

            verify(bookRepository).findById(testBook.getId());
            verify(bookRepository).findByIsbn(testBookDTO2.getIsbn());
            verifyNoMoreInteractions(bookRepository);
            verify(authorRepository).findById(testBookDTO2.getAuthorId());
            verifyNoMoreInteractions(authorRepository);
        }
    }

    @Nested
    @DisplayName("softDeleteBook")
    class softDeleteBook{

        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenIdIsNull() throws BookNotFoundException, NullInputException {
            assertThatThrownBy(() -> bookService.softDeleteBook(null))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameter should be NonNull: id");
        }

        @Test
        @DisplayName("should soft delete the book")
        void shouldSoftDeleteBookSuccessfully() throws BookNotFoundException, NullInputException{
            when(bookRepository.findById(testBook.getId())).thenReturn(Optional.of(testBook));

            bookService.softDeleteBook(testBook.getId());

            assertThat(testBook.getDeleted()).isTrue();

            verify(bookRepository).findById(testBook.getId());
            verifyNoMoreInteractions(bookRepository);

        }

        @Test
        @DisplayName("should not soft delete a book")
        void shouldNotSoftDeleteBookSuccessfully() throws BookNotFoundException{
            when(bookRepository.findById(testBook.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.softDeleteBook(testBook.getId()))
                .isInstanceOf(BookService.BookNotFoundException.class)
                .hasMessageContaining("Book doesn't exist with id: " + testBook.getId());

            verify(bookRepository).findById(testBook.getId());
            verifyNoMoreInteractions(bookRepository);

        }
    }

    @Nested
    @DisplayName("deleteBook")
    class deleteBook{

        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenIdIsNull() throws BookNotFoundException, NullInputException {
            assertThatThrownBy(() -> bookService.deleteBook(null))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameter should be NonNull: id");
        }

        @Test
        @DisplayName("should successfully delete a book")
        void shouldDeleteBookSuccessfully() throws BookNotFoundException, NullInputException{
            when(bookRepository.existsById(testBook.getId())).thenReturn(true);

            bookService.deleteBook(testBook.getId());


            verify(bookRepository).existsById(testBook.getId());
            verify(bookRepository).deleteById(testBook.getId());
            verifyNoMoreInteractions(bookRepository);
        }

        @Test
        @DisplayName("should not successfully delete a book")
        void shouldNotDeleteBookSuccessfully() throws BookNotFoundException, NullInputException{
            when(bookRepository.existsById(testBook.getId())).thenReturn(false);

            assertThatThrownBy(() -> bookService.deleteBook(testBook.getId()))
                .isInstanceOf(BookService.BookNotFoundException.class)
                .hasMessageContaining("Book doesn't exist with id: " + testBook.getId());

            verify(bookRepository).existsById(testBook.getId());
            verifyNoMoreInteractions(bookRepository);
        }
    }

    @Nested
    @DisplayName("updateAvailableCopies")
    class updateAvailableCopies{

        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenIdAndQuantitiesAreNull() throws BookNotFoundException, NullInputException {
            assertThatThrownBy(() -> bookService.updateAvailableCopies(null, null))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameters should be NonNull: id, quantities");
        }

        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenIdIsNull() throws BookNotFoundException, NullInputException {
            assertThatThrownBy(() -> bookService.updateAvailableCopies(null, 1))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameter should be NonNull: id");
        }

        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenQuantitiesIsNull() throws BookNotFoundException, NullInputException {
            assertThatThrownBy(() -> bookService.updateAvailableCopies(1L, null))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameter should be NonNull: quantities");
        }

        @Test
        @DisplayName("should successfully update the available copies of a book")
        void shouldUpdateAvailableCopiesSuccessfully()throws BookNotFoundException, BookHasNotEnoughCopiesException, BookHasTooManyCopiesException, NullInputException {
            when(bookRepository.findById(testBook.getId())).thenReturn(Optional.of(testBook));

            BookDTO result = bookService.updateAvailableCopies(testBook.getId(), 1);

            testBookDTO.setAvailableCopies(testBookDTO.getAvailableCopies() + 1);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(testBookDTO);

            verify(bookRepository).findById(testBook.getId());
            verifyNoMoreInteractions(bookRepository);
        }

        @Test
        @DisplayName("should not successfully update the available copies of a book, not enough copies")
        void shouldNotUpdateAvailableCopiesSuccessfullyNotEnough()throws BookNotFoundException, BookHasNotEnoughCopiesException, BookHasTooManyCopiesException, NullInputException {
            when(bookRepository.findById(testBook.getId())).thenReturn(Optional.of(testBook));


            assertThatThrownBy(() -> bookService.updateAvailableCopies(testBook.getId(), -11))
                .isInstanceOf(BookService.BookHasNotEnoughCopiesException.class)
                .hasMessageContaining("The book with this id doesn't have enough copies: " + testBook.getId());

            verify(bookRepository).findById(testBook.getId());
            verifyNoMoreInteractions(bookRepository);
        }

        @Test
        @DisplayName("should not successfully update the available copies of a book, too many copies")
        void shouldNotUpdateAvailableCopiesSuccessfullyTooMany()throws BookNotFoundException, BookHasNotEnoughCopiesException, BookHasTooManyCopiesException {
            when(bookRepository.findById(testBook.getId())).thenReturn(Optional.of(testBook));


            assertThatThrownBy(() -> bookService.updateAvailableCopies(testBook.getId(), 11))
                .isInstanceOf(BookService.BookHasTooManyCopiesException.class)
                .hasMessageContaining("The book with this id has more available copies then total: " + testBook.getId());

            verify(bookRepository).findById(testBook.getId());
            verifyNoMoreInteractions(bookRepository);
        }

        @Test
        @DisplayName("should not successfully update the available copies of a book, not found")
        void shouldNotUpdateAvailableCopiesSuccessfullyNotFound()throws BookNotFoundException, BookHasNotEnoughCopiesException, BookHasTooManyCopiesException {
            when(bookRepository.findById(testBook.getId())).thenReturn(Optional.empty());


            assertThatThrownBy(() -> bookService.updateAvailableCopies(testBook.getId(), 11))
                .isInstanceOf(BookService.BookNotFoundException.class)
                .hasMessageContaining("Book doesn't exist with id: " + testBook.getId());

            verify(bookRepository).findById(testBook.getId());
            verifyNoMoreInteractions(bookRepository);
        }

    }
    @Nested
    @DisplayName("update total copies")
    class updateTotalCopies{

        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenIdAndQuantitesAreNull() throws BookNotFoundException, NullInputException {
            assertThatThrownBy(() -> bookService.updateTotalCopies(null, null))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameters should be NonNull: id, quantities");
        }

        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenIdIsNull() throws BookNotFoundException, NullInputException {
            assertThatThrownBy(() -> bookService.updateTotalCopies(null, 1))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameter should be NonNull: id");
        }

        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenQuantitiesIsNull() throws BookNotFoundException, NullInputException {
            assertThatThrownBy(() -> bookService.updateTotalCopies(1L, null))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameter should be NonNull: quantities");
        }

        @Test
        @DisplayName("should successfully update total copies")
        void shouldUpdateTotalCopiesSuccessfully() throws BookNotFoundException, BookHasNotEnoughCopiesException, NullInputException{
            when(bookRepository.findById(testBook.getId())).thenReturn(Optional.of(testBook));

            BookDTO result = bookService.updateTotalCopies(testBook.getId(), 11);

            testBookDTO.setTotalCopies(testBookDTO.getTotalCopies()+11);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(testBookDTO);

            verify(bookRepository).findById(testBook.getId());
            verifyNoMoreInteractions(bookRepository);
        }

        @Test
        @DisplayName("should not successfully update the total copies of a book, not found")
        void shouldNotUpdateTotalCopiesSuccessfullyNotFound()throws BookNotFoundException, BookHasNotEnoughCopiesException, NullInputException {
            when(bookRepository.findById(testBook.getId())).thenReturn(Optional.empty());


            assertThatThrownBy(() -> bookService.updateTotalCopies(testBook.getId(), 11))
                .isInstanceOf(BookService.BookNotFoundException.class)
                .hasMessageContaining("Book doesn't exist with id: " + testBook.getId());

            verify(bookRepository).findById(testBook.getId());
            verifyNoMoreInteractions(bookRepository);
        }

        @Test
        @DisplayName("should not successfully update the total copies of a book, not enough copies")
        void shouldNotUpdateTotalCopiesSuccessfullyNotEnough()throws BookNotFoundException, BookHasNotEnoughCopiesException {
            when(bookRepository.findById(testBook.getId())).thenReturn(Optional.of(testBook));


            assertThatThrownBy(() -> bookService.updateTotalCopies(testBook.getId(), -110))
                .isInstanceOf(BookService.BookHasNotEnoughCopiesException.class)
                .hasMessageContaining("The book with this id doesn't have enough copies: " + testBook.getId());

            verify(bookRepository).findById(testBook.getId());
            verifyNoMoreInteractions(bookRepository);
        }
    }

    @Nested
    @DisplayName("restore book by id")
    class restoreBookById{

        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenIdIsNull() throws BookNotFoundException, NullInputException {
            assertThatThrownBy(() -> bookService.restoreBookById(null))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameter should be NonNull: id");
        }


        @Test
        @DisplayName("should restore book by id successfully")
        void shouldRestoreBookByIdSuccessfully() throws BookNotFoundException, NullInputException {

            testBook.setDeleted(true);
            Book spiedBook = spy(testBook);

            when(bookRepository.findByIdAndDeletedTrue(testBook.getId()))
                    .thenReturn(Optional.of(spiedBook));    // <-- RETURN lo spy!

            BookDTO result = bookService.restoreBookById(testBook.getId());

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(testBookDTO);

            verify(spiedBook).setDeleted(false);

            verify(bookRepository).findByIdAndDeletedTrue(testBook.getId());

            verifyNoMoreInteractions(bookRepository);
        }

        @Test
        @DisplayName("should not restore a book, not found")
        void shouldNotRestoreSuccessfullyNotFound()throws BookNotFoundException {
            when(bookRepository.findByIdAndDeletedTrue(testBook.getId())).thenReturn(Optional.empty());


            assertThatThrownBy(() -> bookService.restoreBookById(testBook.getId()))
                .isInstanceOf(BookService.BookNotFoundException.class)
                .hasMessageContaining("Book doesn't exist with id: " + testBook.getId());

            verify(bookRepository).findByIdAndDeletedTrue(testBook.getId());
            verifyNoMoreInteractions(bookRepository);
        }
    }

    @Nested
    @DisplayName("restore book by Isbn")
    class restoreBookByIsbn{

        @Test
        @DisplayName("should throw an exception if the page is null")
        void shouldThrowNullPointerException_whenIdIsNull() throws BookNotFoundException, NullInputException {
            assertThatThrownBy(() -> bookService.restoreBookByIsbn(null))
                .isInstanceOf(BookService.NullInputException.class)
                .hasMessageContaining("This parameter should be NonNull: Isbn");
        }

        @Test
        @DisplayName("should restore book by Isbn successfully")
        void shouldRestoreBookByIdSuccessfully() throws BookNotFoundException, NullInputException {

            testBook.setDeleted(true);
            Book spiedBook = spy(testBook);

            when(bookRepository.findByIsbnAndDeletedTrue(testBook.getIsbn()))
                    .thenReturn(Optional.of(spiedBook));

            BookDTO result = bookService.restoreBookByIsbn(testBook.getIsbn());

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(testBookDTO);

            verify(spiedBook).setDeleted(false);

            verify(bookRepository).findByIsbnAndDeletedTrue(testBook.getIsbn());

            verifyNoMoreInteractions(bookRepository);
        }

        @Test
        @DisplayName("should not restore a book, not found")
        void shouldNotRestoreSuccessfullyNotFound()throws BookNotFoundException {
            when(bookRepository.findByIsbnAndDeletedTrue(testBook.getIsbn())).thenReturn(Optional.empty());


            assertThatThrownBy(() -> bookService.restoreBookByIsbn(testBook.getIsbn()))
                .isInstanceOf(BookService.BookNotFoundException.class)
                .hasMessageContaining("Book doesn't exist with id: " + testBook.getIsbn());

            verify(bookRepository).findByIsbnAndDeletedTrue(testBook.getIsbn());
            verifyNoMoreInteractions(bookRepository);
        }
    }

}
