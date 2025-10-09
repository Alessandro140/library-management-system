package com.example.library.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;


/**
 * A DTO representing a category.
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CategoryDTO {

    /**
     * The unique identifier of the category.
     */
    public Long id;

    /**
     * The name of the category.
     */
    @NotNull
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    public String name;

    /**
     * The description of the category.
     */
    @Size(max = 10000, message = "Description can be up to 10000 characters long")
    public String description;
}

