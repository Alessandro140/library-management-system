package com.example.library.controller;

import com.example.library.dto.CategoryDTO;
import com.example.library.service.CategoryService;
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
@RequestMapping("/api/categories")
@Tag(name = "Category", description = "The Category API")
public class CategoryController {

    /**
     * The CategoryService instance.
     */
    private final CategoryService categoryService;

    /**
     * Create a new CategoryController.
     *
     * @param categoryService the CategoryService instance
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Get a single category by its id.
     *
     * @param id the id of the category
     * @return the category if found, empty otherwise
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a category by id", description = "Get a single category by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the category",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDTO.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CategoryDTO> getCategoryById(
            @Parameter(description = "ID of the category to retrieve", required = true) @NonNull
            @PathVariable
            Long id
    ) {
        // Get the category by its ID.
        return this.categoryService.getCategoryById(id)
                // Return the category if found.
                .map(ResponseEntity::ok)
                // Return a 404 Not Found response if the category is not found.
                .orElse(ResponseEntity.notFound().build());
    }


    /**
     * Get a paginated list of all categories in the library.
     *
     * @param title    the title to filter by (case-insensitive, partial match, optional)
     * @param category   the category to filter by (case-insensitive, partial match, optional)
     * @param pageable the Pageable information for pagination (optional, default page: 0, size: 20, sort: name, direction: ASC)
     * @return a paginated list of categories
     */
    @GetMapping
    @Operation(summary = "List all categories", description = "Get a paginated list of all categories in the library.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of categories",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<CategoryDTO>> getAllCategory(
            @Parameter(description = "Pageable information for pagination") @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) @NotNull
            Pageable pageable
    ) {

        return ResponseEntity.ok(this.categoryService.getCategories(null, pageable));
    }

    /**
     * Create a new category in the library.
     *
     * @param categoryDTO the category to create
     * @return the created category
     */
    @PostMapping
    @Operation(summary = "Create a new category", description = "Create a new category in the library")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully created the category",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
                })
    public ResponseEntity<?> createCategory(
            @Parameter(description = "Category to add to the library", required = true) @NonNull
            @Valid @RequestBody CategoryDTO categoryDTO) {


         return ResponseEntity.ok(this.categoryService.createCategory(categoryDTO));

    }

        /**
         * Update an existing category in the library.
         *
         * @param id      the id of the category to update
         * @param categoryDTO the category data to update
         * @return the updated category
         */
        @PutMapping("/{id}")
        @Operation(summary = "Update a category", description = "Update an existing category in the library")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Successfully updated the category",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDTO.class))),
                @ApiResponse(responseCode = "400", description = "Invalid input",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "Category not found",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<?> updateCategory(
                @Parameter(description = "ID of the category to update", required = true) @NonNull
                @PathVariable
                Long id,
                @Parameter(description = "Updated category information", required = true) @NonNull
                @Valid @RequestBody
                CategoryDTO categoryDTO
        ) {
            try {
                return ResponseEntity.ok(this.categoryService.updateCategory(id, categoryDTO));
            } catch (CategoryService.CategoryNotFoundException e) {
                return e.toResponseEntity();
            }
        }

    /**
     * Delete a category from the library.
     *
     * @param id the id of the category to delete
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category", description = "Delete a category from the library")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Successfully deleted the category"),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deleteCategory(
            @Parameter(description = "ID of the category to delete") @NonNull
            @PathVariable Long id
    ) {
        try {
            this.categoryService.softDeleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (CategoryService.CategoryNotFoundException e) {
            return e.toResponseEntity();
        }
    }

}