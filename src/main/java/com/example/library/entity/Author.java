package com.example.library.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/*
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
@Table(name = "authors")
public class Author extends Auditable {

    /*
     * The unique identifier of the author.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * The first name of the author.
     */
    @Column(nullable = false, length = 255)
    private String firstName;

    /*
     * The last name of the author.
     */
    @Column(nullable = false, length = 255)
    private String lastName;

    /*
     * The birth date of the author.
     */
    @Column(nullable = false)
    private LocalDate birthDate;
}
