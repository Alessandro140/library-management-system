package com.example.library.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

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
     * The email of the user. Must be unique.
     */
    @NotNull(message = "Email must be not null")
    private String email;

    /**
     * The role of the user (e.g., ADMIN).
     */
    @NotNull(message = "User role cannot be null")
    public String role;
}
