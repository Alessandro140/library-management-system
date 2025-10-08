package com.example.library.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/*
 * Represents a category in the library system.
 * This entity is mapped to the 'categories' table in the database.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@Builder
@Table(name = "categories")
public class Category extends Auditable {

    /*
     * The unique identifier of the category.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * The name of the category.
     */
    @Column(nullable = false, length = 255)
    private String name;

    /*
     * The description of the category. Can be up to 10000 characters long.
     * This field is optional.
     */
    @Column(length = 10000)
    private String description;

    /*
     * The books that belong to this category.
     */
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "categories")
    private final Set<Book> books = new HashSet<>();

    /*
    * Indicates whether the category is deleted (soft delete).
    */
    @Builder.Default
    private Boolean isDeleted = false;

}