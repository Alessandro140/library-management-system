package com.example.library.controller;

import com.example.library.dto.BookDTO;
import com.example.library.entity.Book;
import com.example.library.service.BookService;
import com.example.library.specification.BookSpecification;
import com.example.library.utils.SpecificationComposer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Book", description = "The Book API")
public class BookController {

    /**
     * The BookService instance.
     */
    private final BookService bookService;

    /**
     * Create a new BookController.
     *
     * @param bookService the BookService instance
     */
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Get a single book by its id.
     *
     * @param id the id of the book
     * @return the book if found, empty otherwise
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a book by id", description = "Get a single book by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the book",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDTO.class))),
            @ApiResponse(responseCode = "404", description = "Book not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BookDTO> getBookById(
            @Parameter(description = "ID of the book to retrieve", required = true) @NonNull
            @PathVariable
            Long id
    ) {
        // Get the book by its ID.
        return this.bookService.getBookById(id)
                // Return the book if found.
                .map(ResponseEntity::ok)
                // Return a 404 Not Found response if the book is not found.
                .orElse(ResponseEntity.notFound().build());
    }


    /**
     * Get a paginated list of all books in the library. Optional filtering by title and author.
     *
     * @param title    the title to filter by (case-insensitive, partial match, optional)
     * @param author   the author to filter by (case-insensitive, partial match, optional)
     * @param pageable the Pageable information for pagination (optional, default page: 0, size: 20, sort: title, direction: ASC)
     * @return a paginated list of books
     */
    @GetMapping
    @Operation(summary = "List all books", description = "Get a paginated list of all books in the library.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of books",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @Parameter(description = "Filter books by title (case-insensitive, partial match)")
            @RequestParam(required = false) @Nullable
            String title,
            @Parameter(description = "Pageable information for pagination") @ParameterObject
            @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) @NotNull
            Pageable pageable
    ) {
        Specification<Book> bookSpecification = BookSpecification.titleLike(title);
        return ResponseEntity.ok(this.bookService.getBooks(bookSpecification, pageable));
    }

    /**
     * Create a new book in the library.
     *
     * @param bookDTO the book to create
     * @return the created book
     */
    @PostMapping
    @Operation(summary = "Create a new book", description = "Create a new book in the library")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully created the book",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Book already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Author not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
                })
    public ResponseEntity<?> createBook(
            @Parameter(description = "Book to add to the library", required = true) @NonNull
            @Valid @RequestBody BookDTO bookDTO) {

        try {
            // Create the book and return it.
            return ResponseEntity.ok(this.bookService.createBook(bookDTO));
        } catch (BookService.BookAlreadyExistsException | BookService.AuthorNotFoundException e) {
            return e.toResponseEntity();
        }
    }

        /**
         * Update an existing book in the library.
         *
         * @param id      the id of the book to update
         * @param bookDTO the book data to update
         * @return the updated book
         */
        @PutMapping("/{id}")
        @Operation(summary = "Update a book", description = "Update an existing book in the library")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Successfully updated the book",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDTO.class))),
                @ApiResponse(responseCode = "400", description = "Invalid input",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "Book not found",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "Author not found",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<?> updateBook(
                @Parameter(description = "ID of the book to update", required = true) @NonNull
                @PathVariable
                Long id,
                @Parameter(description = "Updated book information", required = true) @NonNull
                @Valid @RequestBody
                BookDTO bookDTO
        ) {
            try {
                // Update the book by its ID and return it.
                // Add a check that there are no book with the same ISBN
                return ResponseEntity.ok(this.bookService.updateBook(id, bookDTO));
            } catch (BookService.BookNotFoundException | BookService.AuthorNotFoundException | BookService.BookAlreadyExistsException e) {
                // Return a 404 Not Found response if the book is not found.
                return e.toResponseEntity();
            }
        }

    /**
     * Delete a book from the library.
     *
     * @param id the id of the book to delete
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book", description = "Delete a book from the library")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Successfully deleted the book"),
            @ApiResponse(responseCode = "404", description = "Book not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deleteBook(
            @Parameter(description = "ID of the book to delete") @NonNull
            @PathVariable Long id
    ) {
        try {
            this.bookService.softDeleteBook(id);
            return ResponseEntity.noContent().build();
        } catch (BookService.BookNotFoundException e) {
            return e.toResponseEntity();
        }
    }


}
