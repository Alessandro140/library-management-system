package com.example.library.controller;

import com.example.library.dto.AuthorDTO;
import com.example.library.service.AuthorService;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/authors")
@Tag(name = "Author", description = "The Author API")

public class AuthorController {

    /**
     * The AuthorService instance.
     */
    private final AuthorService authorService;

    /**
     * Create a new AuthorController.
     *
     * @param authorService the AuthorService instance
     */
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    /**
     * Get a single author by its id.
     *
     * @param id the id of the author
     * @return the author if found, empty otherwise
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a author by id", description = "Get a single author by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the author",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Author not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthorDTO> getAuthorById(
            @Parameter(description = "ID of the author to retrieve", required = true) @NonNull
            @PathVariable
            Long id
    ) {
        // Get the author by its ID.
        return this.authorService.getAuthorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all authors", description = "Get a paginated list of all authors in the library.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of authors",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<AuthorDTO>> getAllAuthors(
            @Parameter(description = "Pageable information for pagination") @ParameterObject
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) @NotNull
            Pageable pageable
    ) {

        return ResponseEntity.ok(this.authorService.getAuthors(null, pageable));
    }


    /**
     * Create a new author in the library.
     *
     * @param authorDTO the author to create
     * @return the created author
     */
    @PostMapping
    @Operation(summary = "Create a new author", description = "Create a new author in the library")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully created the author",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
                })
    public ResponseEntity<?> createAuthor(
            @Parameter(description = "Author to add to the library", required = true) @NonNull
            @Valid @RequestBody AuthorDTO authorDTO) {

            return ResponseEntity.ok(this.authorService.createAuthor(authorDTO));

    }

        /**
         * Update an existing author in the library.
         *
         * @param id      the id of the author to update
         * @param authorDTO the author data to update
         * @return the updated author
         */
        @PutMapping("/{id}")
        @Operation(summary = "Update a author", description = "Update an existing author in the library")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Successfully updated the author",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorDTO.class))),
                @ApiResponse(responseCode = "400", description = "Invalid input",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "Author not found",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "Author not found",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<?> updateAuthor(
                @Parameter(description = "ID of the author to update", required = true) @NonNull
                @PathVariable
                Long id,
                @Parameter(description = "Updated author information", required = true) @NonNull
                @Valid @RequestBody
                AuthorDTO authorDTO
        ) {
            try {
                return ResponseEntity.ok(this.authorService.updateAuthor(id, authorDTO));
            } catch (AuthorService.AuthorNotFoundException e) {
                return e.toResponseEntity();
            }
        }

    /**
     * Delete a author from the library.
     *
     * @param id the id of the author to delete
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a author", description = "Delete a author from the library")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Successfully deleted the author"),
            @ApiResponse(responseCode = "404", description = "Author not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deleteAuthor(
            @Parameter(description = "ID of the author to delete") @NonNull
            @PathVariable Long id
    ) {
        try {
            this.authorService.softDeleteAuthor(id);
            return ResponseEntity.noContent().build();
        } catch (AuthorService.AuthorNotFoundException e) {
            return e.toResponseEntity();
        }
    }

}
