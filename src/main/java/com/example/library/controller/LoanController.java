package com.example.library.controller;

import com.example.library.dto.LoanDTO;
import com.example.library.service.LoanService;
import com.example.library.service.LoanService;
import com.example.library.service.BookService;
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
import java.util.Optional;


@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loan", description = "The Loan API")
public class LoanController {

    /**
     * The LoanService instance.
     */
    private final LoanService loanService;
    private final BookService bookService;
    /**
     * Create a new LoanController.
     *
     * @param loanService the LoanService instance
     * @param bookService the BookService instance
     */
    public LoanController(LoanService loanService, BookService bookService) {
        this.loanService = loanService;
        this.bookService = bookService;
    }

    /**
     * Get a single loan by its id.
     *
     * @param id the id of the loan
     * @return the loan if found, empty otherwise
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a loan by id", description = "Get a single loan by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the loan",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoanDTO.class))),
            @ApiResponse(responseCode = "404", description = "Loan not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoanDTO> getLoanById(
            @Parameter(description = "ID of the loan to retrieve", required = true) @NonNull
            @PathVariable
            Long id
    ) {
        // Get the loan by its ID.
        return this.loanService.getLoanById(id)
                // Return the loan if found.
                .map(ResponseEntity::ok)
                // Return a 404 Not Found response if the loan is not found.
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get a paginated list of all loans in the library.
     * @param title    the title to filter by (case-insensitive, partial match, optional)
     * @param loan   the loan to filter by (case-insensitive, partial match, optional)
     * @param pageable the Pageable information for pagination (optional, default page: 0, size: 20, sort: name, direction: ASC)
     * @return a paginated list of loans
     */
    @GetMapping
    @Operation(summary = "List all loans", description = "Get a paginated list of all loans in the library.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of loans",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<LoanDTO>> getAllLoan(
            @Parameter(description = "Pageable information for pagination") @ParameterObject
            @PageableDefault(size = 20, sort = "status", direction = Sort.Direction.ASC) @NotNull
            Pageable pageable) {

        return ResponseEntity.ok(this.loanService.getLoans(null, pageable));
    }

    /**
     * Create a new loan in the library.
     *
     * @param loanDTO the loan to create
     * @return the created loan
     */
    @PostMapping
    @Operation(summary = "Create a new loan", description = "Create a new loan in the library")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully created the loan",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoanDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Loan not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "One of the book is not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "One of the book has not enough copies",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
                })
    public ResponseEntity<?> createLoan(
            @Parameter(description = "Loan to add to the library", required = true) @NonNull
            @Valid @RequestBody LoanDTO loanDTO) {

    try {
        // validazione base
        if (loanDTO.getBooks() == null || loanDTO.getBooks().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid input");
        }

            return ResponseEntity.ok(this.loanService.createLoan(loanDTO));

        } catch (LoanService.UserNotFoundException | LoanService.BookNotFoundException |
                LoanService.BookHasNotEnoughCopiesException e) {
            return e.toResponseEntity();
        }
    }

    /**
     * Delete a loan from the library.
     *
     * @param id the id of the loan to delete
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a loan", description = "Delete a loan from the library")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Successfully deleted the loan"),
            @ApiResponse(responseCode = "404", description = "Loan not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deleteLoan(
            @Parameter(description = "ID of the loan to delete") @NonNull
            @PathVariable Long id
    ) {
        try {
            this.loanService.softDeleteLoan(id);
            return ResponseEntity.noContent().build();
        } catch (LoanService.LoanNotFoundException e) {
            return e.toResponseEntity();
        }
    }

}
