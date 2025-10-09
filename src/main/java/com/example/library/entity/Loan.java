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

/**
 * Represents a loan in the library system.
 * This entity is mapped to the 'loan' table in the database.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "loans")
public class Loan extends Auditable {

    /**
     * The unique identifier of the loan.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The books that are associated with this loan.
     */
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "loans")
    private final Set<Book> books = new HashSet<>();

    /**
     * The user who made the loan.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    /**
     * The date when the loan was made.
     */
    @Column(nullable = false)
    private LocalDate loanDate;

    /**
     * The due date for returning the loaned books.
     */
    @Column(nullable = false)
    private LocalDate dueDate;

    /**
     * The status of the loan.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    /**
    * Indicates whether the book is deleted (soft delete).
    */
    private Boolean deleted = false;

}
