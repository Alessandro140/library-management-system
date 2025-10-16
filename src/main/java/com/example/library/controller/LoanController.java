package com.example.library.controller;

import com.example.library.dto.LoanDTO;
import com.example.library.entity.Loan;
import com.example.library.service.LoanService;
import com.example.library.specification.LoanSpecification;

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
@RequestMapping("/api/loans")
@Tag(name = "Loan", description = "The Loan API")
public class LoanController {

    /**
     * The LoanService instance.
     */
    private final LoanService loanService;
    /**
     * Create a new LoanController.
     *
     * @param loanService the LoanService instance
     * @param bookService the BookService instance
     */
    public LoanController(LoanService loanService) {
        this.loanService = loanService;
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Inputs are null",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	})
    public ResponseEntity<?> getLoanById(
            @Parameter(description = "ID of the loan to retrieve", required = true) @NonNull
            @PathVariable
            Long id
    ) {
		try{
			// Get the loan by its ID.
			return this.loanService.getLoanById(id)
					// Return the loan if found.
					.map(ResponseEntity::ok)
					// Return a 404 Not Found response if the loan is not found.
					.orElse(ResponseEntity.notFound().build());
		} catch(LoanService.NullInputException e){
			return e.toResponseEntity();
		}

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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Inputs are null",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	})
    public ResponseEntity<?> getAllLoan(
            @Parameter(description = "Filter Loans by status (case-insensitive, partial match)")
            @RequestParam(required = false) @Nullable
            String status,
            @Parameter(description = "Pageable information for pagination") @ParameterObject
            @PageableDefault(size = 20, sort = "status", direction = Sort.Direction.ASC) @NotNull
            Pageable pageable) {

		try{
        	Specification<Loan> loanSpecification = LoanSpecification.statusLike(status);
        	return ResponseEntity.ok(this.loanService.getLoans(loanSpecification, pageable));
		} catch (LoanService.NullInputException e){
			return e.toResponseEntity();
		}
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Inputs are null",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	})
    public ResponseEntity<?> createLoan(
            @Parameter(description = "Loan to add to the library", required = true) @NonNull
            @Valid @RequestBody LoanDTO loanDTO) {

	    try {
            return ResponseEntity.ok(this.loanService.createLoan(loanDTO));
        } catch (LoanService.BookNotFoundException |LoanService.BookHasNotEnoughCopiesException
                        | LoanService.UserNotFoundException | LoanService.NullInputException e) {
            return e.toResponseEntity();
        }
    }

	/**
	 * Update an existing loan in the library.
	 *
	 * @param id      the id of the loan to update
	 * @param loanDTO the loan data to update
	 * @return the updated loan
	 */
	@PutMapping("/{id}")
	@Operation(summary = "Update a loan", description = "Update an existing loan in the library")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Successfully updated the loan",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoanDTO.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "Loan not found",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "Book not found",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "User not found",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "Book has not enough copies",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "Inputs are null",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	})
	public ResponseEntity<?> updateLoan(
			@Parameter(description = "ID of the loan to update", required = true) @NonNull
			@PathVariable
			Long id,
			@Parameter(description = "Updated loan information", required = true) @NonNull
			@Valid @RequestBody
			LoanDTO loanDTO
	) {
		try {
			return ResponseEntity.ok(this.loanService.updateLoan(id, loanDTO));
		} catch (LoanService.LoanNotFoundException | LoanService.UserNotFoundException |
			LoanService.BookHasNotEnoughCopiesException | LoanService.BookNotFoundException
			| LoanService.NullInputException | LoanService.BookHasTooManyCopiesException e) {
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Inputs are null",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deleteLoan(
            @Parameter(description = "ID of the loan to delete") @NonNull
            @PathVariable Long id
    ) {
        try {
            this.loanService.softDeleteLoan(id);
            return ResponseEntity.noContent().build();
        } catch (LoanService.LoanNotFoundException | LoanService.NullInputException |
				LoanService.BookHasTooManyCopiesException | LoanService.BookHasNotEnoughCopiesException e) {
            return e.toResponseEntity();
        }
    }

}
