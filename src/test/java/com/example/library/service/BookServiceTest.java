package com.example.library.service;

import com.example.library.entity.Author;
import com.example.library.entity.Category;
import com.example.library.entity.Loan;
import com.example.library.dto.BookDTO;
import com.example.library.entity.Book;
import com.example.library.mapper.BookMapper;
import com.example.library.repository.BookRepository;
import com.example.library.service.BookService.BookAlreadyExistsException;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Tests")
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    private final BookMapper bookMapper = Mappers.getMapper(BookMapper.class);

    private BookService bookService;

    private Author testAuthor;
    private Book testBook;
    private Book testBook2;
    private BookDTO testBookDTO;
    private BookDTO testBookDTO2;
    private Category category1, category2;
    private Loan loan1;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, bookMapper);

        // Author
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setFirstName("J.R.R.");
        testAuthor.setLastName("Tolkien");
        testAuthor.setBirthDate(LocalDate.of(1892, 1, 3));

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
        testBook.setISBN("1234567890123");
        testBook.setAuthor(testAuthor);
        testBook.setTotal_copies(10);
        testBook.setAvailable_copies(5);
        testBook.setPublished_date(LocalDate.of(2020, 1, 1));
        testBook.getCategories().add(category1);
        testBook.getCategories().add(category2);
        testBook.getLoans().add(loan1);

        // Book 2
        testBook2 = new Book();
        testBook2.setId(2L);
        testBook2.setTitle("The Hobbit");
        testBook2.setDescription("A book used for test");
        testBook2.setISBN("1234567890125");
        testBook2.setAuthor(testAuthor);
        testBook2.setTotal_copies(8);
        testBook2.setAvailable_copies(6);
        testBook2.setPublished_date(LocalDate.of(2020, 1, 1));
        testBook2.getCategories().add(category1);
        testBook2.getCategories().add(category2);
        testBook2.getLoans().add(loan1);

        // DTOs
        testBookDTO = new BookDTO(
            testBook.getId(), testBook.getTitle(), testBook.getDescription(),
            testBook.getAvailable_copies(), testBook.getTotal_copies(),
            testBook.getISBN(), testBook.getAuthor().getId(), testBook.getPublished_date()
        );

        testBookDTO2 = new BookDTO(
            testBook2.getId(), testBook2.getTitle(), testBook2.getDescription(),
            testBook2.getAvailable_copies(), testBook2.getTotal_copies(),
            testBook2.getISBN(), testBook2.getAuthor().getId(), testBook2.getPublished_date()
        );
    }

    @AfterEach
    void tearDown() {
        testAuthor = null;
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
        void shouldReturnBookWhenFound() {
            when(bookRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testBook));

            Optional<BookDTO> result = bookService.getBookById(1L);

            assertThat(result).isPresent();
            assertThat(result.get()).usingRecursiveComparison().isEqualTo(testBookDTO);
            verify(bookRepository).findByIdAndDeletedFalse(1L);
        }

        @Test
        @DisplayName("should return empty when book not found")
        void shouldNotReturnBookWhenFound() {
            when(bookRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.empty());

            Optional<BookDTO> result = bookService.getBookById(2L);

            assertThat(result).isEmpty();
            verify(bookRepository).findByIdAndDeletedFalse(2L);
        }
    }

    @Nested
    @DisplayName("getBooks")
    class GetBooks {

        @Test
        @DisplayName("should return paged books")
        void shouldReturnPagedBooks() {
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
        @DisplayName("should create book successfully")
        void shouldCreateBookSuccessfully() throws BookService.BookAlreadyExistsException{
        	when(bookRepository.findByISBNAndDeletedFalse(testBookDTO2.getISBN())).thenReturn(Optional.empty());
            when(bookRepository.save(any(Book.class))).thenReturn(testBook2);

            BookDTO result = bookService.createBook(testBookDTO2);

            testBookDTO2.setId(2L);
            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(testBookDTO2);
			verify(bookRepository).save(any(Book.class));
			verify(bookRepository).findByISBNAndDeletedFalse(testBookDTO2.getISBN());
        }

        @Test
        @DisplayName("should not create book successfully")
        void shouldNotCreateBookSuccessfully() throws BookService.BookAlreadyExistsException {
            // Stub per simulare libro già esistente
            when(bookRepository.findByISBNAndDeletedFalse(testBookDTO2.getISBN())).thenReturn(Optional.of(testBook2));

            // Verifica che venga lanciata l'eccezione
            assertThatThrownBy(() -> bookService.createBook(testBookDTO2))
                .isInstanceOf(BookService.BookAlreadyExistsException.class)
                .hasMessageContaining("Book already exists with ISBN: " + testBookDTO2.getISBN());

            // Verifica le interazioni
            verify(bookRepository).findByISBNAndDeletedFalse(testBookDTO2.getISBN());
            verifyNoMoreInteractions(bookRepository);
        }

    }


}
