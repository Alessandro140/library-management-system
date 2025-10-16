package com.example.library.service;

import com.example.library.dto.AuthorDTO;
import com.example.library.entity.Author;
import com.example.library.mapper.AuthorMapper;
import com.example.library.mapper.BookMapper;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorService Tests with MapStruct")
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    private AuthorMapper authorMapper;
    private AuthorService authorService;
    private Author testAuthor;
    private AuthorDTO testAuthorDTO;

    @BeforeEach
    void setUp() {
        // ottieni l'implementazione reale generata da MapStruct
        authorMapper = Mappers.getMapper(AuthorMapper.class);

        authorService = new AuthorService(authorRepository, authorMapper);

        testAuthor = new Author(1L, "Aldo", "Bianchi", LocalDate.of(1990, 2, 3), new HashSet<>(), false);
        testAuthorDTO = authorMapper.toDto(testAuthor);
    }

    @Nested
    @DisplayName("getAuthorById")
    class GetAuthorById {

        @Test
        @DisplayName("should return author when found")
        void shouldReturnAuthorWhenFound() throws AuthorService.NullInputException{
            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));

            Optional<AuthorDTO> result = authorService.getAuthorById(1L);

            assertThat(result).isPresent();
            assertThat(result.get()).usingRecursiveComparison().isEqualTo(testAuthorDTO);
            verify(authorRepository).findById(1L);
        }

        @Test
        @DisplayName("should return empty when author not found")
        void shouldReturnEmptyWhenAuthorNotFound() throws AuthorService.NullInputException {
            when(authorRepository.findById(1L)).thenReturn(Optional.empty());

            Optional<AuthorDTO> result = authorService.getAuthorById(1L);

            assertThat(result).isEmpty();
            verify(authorRepository).findById(1L);
        }
    }
}
