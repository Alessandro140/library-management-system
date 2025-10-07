package com.example.library.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


/**
 * A DTO representing an author.
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AuthorDTO{

    /*
     * The unique identifier of the author.
     */
    public Long id;

    /*
     * The first name of the author.
     */
    @NotNull
    @Size(min = 1, max = 255, message = "First name must be between 1 and 255 characters")
    public String firstName;

    /*
     * The last name of the author.
     */
    @NotNull
    @Size(min = 1, max = 255, message = "Last name must be between 1 and 255 characters")
    public String lastName;

    /*
     * The birth date of the author.
     */
    @NotNull
    @Past(message = "Birth date must be in the past")
    public LocalDate birthDate;

    /*
     * The books written by the author.
     */
    public Set<BookDTO> books = new HashSet<>();
}