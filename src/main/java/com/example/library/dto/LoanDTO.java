package com.example.library.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.library.entity.LoanStatus;

/**
 * A DTO representing an author.
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LoanDTO {

    /**
     * The unique identifier of the loan.
     */
    public Long id;

    /**
     * The user who made the loan.
     */
    @NotNull
    public Long userId;

    /**
     * The date when the loan was made.
     */
    @NotNull
    @PastOrPresent(message = "Loan date must be in the past or present")
    public LocalDate loanDate;

    /**
     * The due date for returning the loaned books.
     */
    @NotNull
    public LocalDate dueDate;

    /**
     * The status of the loan.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    public LoanStatus status;

    /**
     * The books associated with the loan
     */
    @NotNull
    public List<BookDTO> books = new ArrayList<BookDTO>();

}
