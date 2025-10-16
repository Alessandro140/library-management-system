package com.example.library.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Where;

/**
 * Represents an author in the library system.
 * This entity is mapped to the 'authors' table in the database.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@Where(clause = "deleted=false")
@Table(name = "authors")
public class Author extends Auditable {

    /**
     * The unique identifier of the author.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The first name of the author.
     */
    @Column(nullable = false, length = 255)
    private String firstName;

    /**
     * The last name of the author.
     */
    @Column(nullable = false, length = 255)
    private String lastName;

    /**
     * The birth date of the author.
     */
    @Column(nullable = false)
    private LocalDate birthDate;
    /**
     * The books written by the author.
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private Set<Book> books = new HashSet<>();
    /**
     * Indicates whether the author is deleted (soft delete).
     */
    private Boolean deleted = false;
}
