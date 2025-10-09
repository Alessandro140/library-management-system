package com.example.library.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;


/**
 * A DTO representing a book.
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BookDTO {

    /**
     * The unique identifier of the book.
     */
    public Long id;

    /**
     * The title of the book.
     */
    @NotNull
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    public String title;

    /**
     * The description of the book. Can be up to 10000 characters long.
     * This field is optional.
     */
    @Size(max = 10000, message = "Description can be up to 10000 characters long")
    public String description;
    /**
     * The available number of copies of the book in the library.
     */
    @NotNull
    public int available_copies;
    /**
     * The total number of copies of the book in the library.
     */
    @NotNull
    public int total_copies;
    /**
     * The ISBN of the book. Must be unique and 13 characters long.
     */
    @NotNull
    @Size(min = 13, max = 13, message = "ISBN must be exactly 13 characters long")
    public String ISBN;

    /**
     * The author of the book.
     */
    @NotNull(message = "Author cannot be null")
    public long authorId;

    /**
     * Published date of the book.
     */
    @NotNull(message = "Published date cannot be null")
    public LocalDate published_date;

}
