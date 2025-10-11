package com.example.library.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.library.entity.Loan;

/*
 * Repository for the Loan entity.
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    /**
     *
	 * Find an loan by its id.
	 *
	 * @param id the id of the loan
	 * @return an optional with the loan if found, empty otherwise
	 */
	Optional<Loan> findByIdAndDeletedFalse(Long id);

	/**
	 * Find a soft deleted loan by its id.
	 *
	 * @param id the id of the loan
	 * @return an optional with the loan if found, empty otherwise
	 */
	Optional<Loan> findByIdAndDeletedTrue(Long id);

}
