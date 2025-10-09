package com.example.library.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


/**
 * A DTO representing a user in the library system.
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UserDTO {

    /**
     * The unique identifier of the user.
     */
    public Long id;

    /**
     * The username of the user.
     */
    @NotNull
    @Size(min = 1, max = 255, message = "Username must be between 1 and 255 characters")
    public String username;

    /**
     * The role of the user (e.g., ADMIN).
     */
    @NotNull(message = "User role cannot be null")
    public String role;

    private final Set<LoanDTO> loans = new HashSet<>();
}
