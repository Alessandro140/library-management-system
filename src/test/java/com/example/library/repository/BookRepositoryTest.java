package com.example.library.repository;

import com.example.library.entity.Author;
import com.example.library.entity.Book;
import com.example.library.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Book Repository Tests")
class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private Book testBook;
    private Author testAuthor;
    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        // 1) crea e persisti l'autore
        testAuthor = new Author();
        testAuthor.setFirstName("J.R.R.");
        testAuthor.setLastName("Tolkien");
        testAuthor.setBirthDate(LocalDate.of(1892, 1, 3));
        testAuthor = entityManager.persist(testAuthor);

        // 2) crea e persisti le categorie
        category1 = new Category();
        category1.setName("Fantasy");
        category1 = entityManager.persist(category1);

        category2 = new Category();
        category2.setName("Adventure");
        category2 = entityManager.persist(category2);

        // 3) crea e popola il book (TUTTI i campi NOT NULL devono essere valorizzati)
        testBook = new Book();
        testBook.setTitle("LOTR");
        testBook.setDescription("A book used for test");
        testBook.setIsbn("1234567890123"); // 13 chars, non-null
        testBook.setAuthor(testAuthor); // Author già persist
        testBook.setTotalCopies(10);
        testBook.setAvailableCopies(5);
        testBook.setPublishedDate(LocalDate.of(2020, 1, 1));
        testBook.getCategories().add(category1);
        testBook.getCategories().add(category2);
        testBook.setDeleted(false);

        // 4) persisti il book dopo averlo popolato
        testBook = entityManager.persist(testBook);

        // 5) flush per forzare SQL immediato (utile per debug/assert)
        entityManager.flush();
    }

    @Nested
    @DisplayName("Find operations")
    class FindOperations {

        @Test
        @DisplayName("Find book by ID")
        void whenFindById_thenReturnBook() {
            Optional<Book> foundOpt = bookRepository.findById(testBook.getId());

            assertThat(foundOpt).isPresent();
            Book actual = foundOpt.get();

            assertThat(actual.getTitle()).isEqualTo(testBook.getTitle());
            assertThat(actual.getDescription()).isEqualTo(testBook.getDescription());
            assertThat(actual.getIsbn()).isEqualTo(testBook.getIsbn());

            assertThat(actual.getAuthor()).isNotNull();
            assertThat(actual.getAuthor().getId()).isEqualTo(testAuthor.getId());

            assertThat(actual.getTotalCopies()).isEqualTo(testBook.getTotalCopies());
            assertThat(actual.getAvailableCopies()).isEqualTo(testBook.getAvailableCopies());
            assertThat(actual.getPublishedDate()).isEqualTo(testBook.getPublishedDate());

            Set<Long> expectedCatIds = testBook.getCategories().stream()
                    .map(Category::getId).collect(Collectors.toSet());
            Set<Long> actualCatIds = actual.getCategories().stream()
                    .map(Category::getId).collect(Collectors.toSet());
            assertThat(actualCatIds).containsExactlyInAnyOrderElementsOf(expectedCatIds);

            assertThat(actual.getDeleted()).isEqualTo(testBook.getDeleted());
        }

        @Test
        @DisplayName("Find not deleted book by ID")
        void whenFindByIdAndDeletedFalse_thenReturnBook() {

            Optional<Book> foundOpt = bookRepository.findByIdAndDeletedFalse(testBook.getId());
            assertThat(foundOpt).isPresent();
            Book actual = foundOpt.get();

            assertThat(actual.getTitle()).isEqualTo(testBook.getTitle());
            assertThat(actual.getDescription()).isEqualTo(testBook.getDescription());
            assertThat(actual.getIsbn()).isEqualTo(testBook.getIsbn());

            assertThat(actual.getAuthor()).isNotNull();
            assertThat(actual.getAuthor().getId()).isEqualTo(testAuthor.getId());

            assertThat(actual.getTotalCopies()).isEqualTo(testBook.getTotalCopies());
            assertThat(actual.getAvailableCopies()).isEqualTo(testBook.getAvailableCopies());
            assertThat(actual.getPublishedDate()).isEqualTo(testBook.getPublishedDate());

            Set<Long> expectedCatIds = testBook.getCategories().stream()
                    .map(Category::getId).collect(Collectors.toSet());
            Set<Long> actualCatIds = actual.getCategories().stream()
                    .map(Category::getId).collect(Collectors.toSet());
            assertThat(actualCatIds).containsExactlyInAnyOrderElementsOf(expectedCatIds);

            assertThat(actual.getDeleted()).isEqualTo(testBook.getDeleted());
        }

        @Test
        @DisplayName("Don't find soft deleted book by ID")
        void whenFindByIdAndDeletedFalse_thenDontReturnBook() {
            testBook.setDeleted(true);
            entityManager.flush();

            Optional<Book> foundOpt = bookRepository.findByIdAndDeletedFalse(testBook.getId());
            assertThat(foundOpt).isEmpty();
        }

        @Test
        @DisplayName("Don't find soft deleted book by ID")
        void whenFindByIdAndDeletedTrue_thenDontReturnBook() {
            Optional<Book> foundOpt = bookRepository.findByIdAndDeletedTrue(testBook.getId());
            assertThat(foundOpt).isEmpty();
        }


        @Test
        @DisplayName("Don't find soft deleted book by ID")
        void whenFindByIdAndDeletedTrue_thenReturnBook() {
            testBook.setDeleted(true);
            entityManager.flush();
            Optional<Book> foundOpt = bookRepository.findByIdAndDeletedTrue(testBook.getId());
            assertThat(foundOpt).isPresent();
            Book actual = foundOpt.get();

            assertThat(actual.getTitle()).isEqualTo(testBook.getTitle());
            assertThat(actual.getDescription()).isEqualTo(testBook.getDescription());
            assertThat(actual.getIsbn()).isEqualTo(testBook.getIsbn());

            assertThat(actual.getAuthor()).isNotNull();
            assertThat(actual.getAuthor().getId()).isEqualTo(testAuthor.getId());

            assertThat(actual.getTotalCopies()).isEqualTo(testBook.getTotalCopies());
            assertThat(actual.getAvailableCopies()).isEqualTo(testBook.getAvailableCopies());
            assertThat(actual.getPublishedDate()).isEqualTo(testBook.getPublishedDate());

            Set<Long> expectedCatIds = testBook.getCategories().stream()
                    .map(Category::getId).collect(Collectors.toSet());
            Set<Long> actualCatIds = actual.getCategories().stream()
                    .map(Category::getId).collect(Collectors.toSet());
            assertThat(actualCatIds).containsExactlyInAnyOrderElementsOf(expectedCatIds);

            assertThat(actual.getDeleted()).isEqualTo(testBook.getDeleted());
        }

        @Test
        @DisplayName("Find not soft deleted book by Isbn")
        void whenFindByIsbnAndDeletedFalse_thenReturnBook() {

            Optional<Book> foundOpt = bookRepository.findByIsbnAndDeletedFalse(testBook.getIsbn());
            assertThat(foundOpt).isPresent();
            Book actual = foundOpt.get();

            assertThat(actual.getTitle()).isEqualTo(testBook.getTitle());
            assertThat(actual.getDescription()).isEqualTo(testBook.getDescription());
            assertThat(actual.getIsbn()).isEqualTo(testBook.getIsbn());

            assertThat(actual.getAuthor()).isNotNull();
            assertThat(actual.getAuthor().getId()).isEqualTo(testAuthor.getId());

            assertThat(actual.getTotalCopies()).isEqualTo(testBook.getTotalCopies());
            assertThat(actual.getAvailableCopies()).isEqualTo(testBook.getAvailableCopies());
            assertThat(actual.getPublishedDate()).isEqualTo(testBook.getPublishedDate());

            Set<Long> expectedCatIds = testBook.getCategories().stream()
                    .map(Category::getId).collect(Collectors.toSet());
            Set<Long> actualCatIds = actual.getCategories().stream()
                    .map(Category::getId).collect(Collectors.toSet());
            assertThat(actualCatIds).containsExactlyInAnyOrderElementsOf(expectedCatIds);

            assertThat(actual.getDeleted()).isEqualTo(testBook.getDeleted());
        }

        @Test
        @DisplayName("Find soft deleted book by Isbn")
        void whenFindByIsbnAndDeletedTrue_thenReturnBook() {
            testBook.setDeleted(true);
            entityManager.flush();

            Optional<Book> foundOpt = bookRepository.findByIsbnAndDeletedTrue(testBook.getIsbn());
            assertThat(foundOpt).isPresent();
            Book actual = foundOpt.get();

            assertThat(actual.getTitle()).isEqualTo(testBook.getTitle());
            assertThat(actual.getDescription()).isEqualTo(testBook.getDescription());
            assertThat(actual.getIsbn()).isEqualTo(testBook.getIsbn());

            assertThat(actual.getAuthor()).isNotNull();
            assertThat(actual.getAuthor().getId()).isEqualTo(testAuthor.getId());

            assertThat(actual.getTotalCopies()).isEqualTo(testBook.getTotalCopies());
            assertThat(actual.getAvailableCopies()).isEqualTo(testBook.getAvailableCopies());
            assertThat(actual.getPublishedDate()).isEqualTo(testBook.getPublishedDate());

            Set<Long> expectedCatIds = testBook.getCategories().stream()
                    .map(Category::getId).collect(Collectors.toSet());
            Set<Long> actualCatIds = actual.getCategories().stream()
                    .map(Category::getId).collect(Collectors.toSet());
            assertThat(actualCatIds).containsExactlyInAnyOrderElementsOf(expectedCatIds);

            assertThat(actual.getDeleted()).isEqualTo(testBook.getDeleted());
        }

        @Test
        @DisplayName("Not find soft deleted book between non soft deleted by Isbn")
        void whenFindByIsbnAndDeletedFalse_thenNotReturnBook() {
            testBook.setDeleted(true);
            entityManager.flush();

            Optional<Book> foundOpt = bookRepository.findByIsbnAndDeletedFalse(testBook.getIsbn());
            assertThat(foundOpt).isEmpty();
        }

        @Test
        @DisplayName("Not find book between soft deleted by Isbn")
        void whenFindByIsbnAndDeletedTrue_thenNotReturnBook() {
            entityManager.flush();

            Optional<Book> foundOpt = bookRepository.findByIsbnAndDeletedTrue(testBook.getIsbn());
            assertThat(foundOpt).isEmpty();
        }
    }


}
