package com.example.library.controller;

import com.example.library.dto.UserDTO;
import com.example.library.service.UserService;

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
@RequestMapping("/api/users")
@Tag(name = "User", description = "The User API")
public class UserController {

    /**
     * The UserService instance.
     */
    private final UserService userService;

    /**
     * Create a new UserController.
     *
     * @param userService the UserService instance
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get a single user by its id.
     *
     * @param id the id of the user
     * @return the user if found, empty otherwise
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a user by id", description = "Get a single user by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Inputs are null",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getUserById(
            @Parameter(description = "ID of the user to retrieve", required = true) @NonNull
            @PathVariable
            Long id
    ) {
        try{
            // Get the user by its ID.
            return this.userService.getUserById(id)
                    // Return the user if found.
                    .map(ResponseEntity::ok)
                    // Return a 404 Not Found response if the user is not found.
                    .orElse(ResponseEntity.notFound().build());
        } catch (UserService.NullInputException e){
            return e.toResponseEntity();
        }
    }

    /**
     * Get a paginated list of all users in the library.
     * @param title    the title to filter by (case-insensitive, partial match, optional)
     * @param user   the user to filter by (case-insensitive, partial match, optional)
     * @param pageable the Pageable information for pagination (optional, default page: 0, size: 20, sort: name, direction: ASC)
     * @return a paginated list of users
     */
    @GetMapping
    @Operation(summary = "List all users", description = "Get a paginated list of all users in the library.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Inputs are null",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getAllUser(
            @Parameter(description = "Pageable information for pagination") @ParameterObject
            @PageableDefault(size = 20, sort = "email", direction = Sort.Direction.ASC) @NotNull
            Pageable pageable) {

        try{
            return ResponseEntity.ok(this.userService.getUsers(null, pageable));
        } catch(UserService.NullInputException e){
            return e.toResponseEntity();
        }
    }


    /*Not working now => can't put password. Should i create an CreateUserDTO? and a relative mapper?*/
    /**
     * Create a new user in the library.
     *
     * @param userDTO the user to create
     * @return the created user
     */
    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user in the library")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully created the user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Inputs are null",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))

    })
    public ResponseEntity<?> createUser(
            @Parameter(description = "User to add to the library", required = true) @NonNull
            @Valid @RequestBody UserDTO userDTO) {

        try {
            return ResponseEntity.ok(this.userService.createUser(userDTO));
        } catch (UserService.UserAlreadyExistsException | UserService.NullInputException e) {
            return e.toResponseEntity();
        }
    }

    /**
     * Update an existing user in the library.
     *
     * @param id      the id of the user to update
     * @param userDTO the user data to update
     * @return the updated user
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a user", description = "Update an existing user in the library")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully updated the user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "User with this mail already exist",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Inputs are null",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> updateUser(
            @Parameter(description = "ID of the user to update", required = true) @NonNull
            @PathVariable
            Long id,
            @Parameter(description = "Updated user information", required = true) @NonNull
            @Valid @RequestBody
            UserDTO userDTO){
        try {
                return ResponseEntity.ok(this.userService.updateUser(id, userDTO));
        } catch (UserService.UserNotFoundException | UserService.UserAlreadyExistsException
                    | UserService.NullInputException e) {
                return e.toResponseEntity();
        }
    }

    /**
     * Delete a user from the library.
     *
     * @param id the id of the user to delete
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "Delete a user from the library")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Successfully deleted the user"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Inputs are null",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID of the user to delete") @NonNull
            @PathVariable Long id
    ) {
        try {
            this.userService.softDeleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (UserService.UserNotFoundException | UserService.NullInputException e) {
            return e.toResponseEntity();
        }
    }
}
