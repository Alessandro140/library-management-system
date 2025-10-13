package com.example.library.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/*
 * Represents a book in the library system.
 * This entity is mapped to the 'books' table in the database.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"categories", "loans", "author"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "books")
public class Book extends Auditable{

    /**
     * The unique identifier of the book.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The title of the book.
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * The description of the book. Can be up to 10000 characters long.
     * This field is optional.
     */
    @Size(max = 10000)
    private String description;

    /**
     * The ISBN of the book. Must be unique and 13 characters long.
     */
    @Column(nullable = false, length = 13)
    private String isbn;

    /**
     * The author of the book.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    /**
     * The total number of copies of the book in the library.
     */
    @Column(nullable = false)
    private Integer totalCopies;

    /**
     * The number of available copies of the book in the library.
     */
    @Column(nullable = false)
    private Integer availableCopies;

    /**
     * The publication date of the book.
     */
    @Column(nullable = false)
    private LocalDate publishedDate;

    /**
     * The categories that the book belongs to.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_category",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private final Set<Category> categories = new HashSet<>();

    /**
     * The loans that the book is associated with.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "loans_book",
            joinColumns = @JoinColumn(name = "book"),
            inverseJoinColumns = @JoinColumn(name = "loan")
    )
    private final Set<Loan> loans = new HashSet<>();
    /**
    * Indicates whether the book is deleted (soft delete).
    */
    private Boolean deleted = false;
}
