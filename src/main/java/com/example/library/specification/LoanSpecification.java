package com.example.library.specification;

import com.example.library.entity.Loan;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

/**
 * Defines specifications for querying loans.
 */
public interface LoanSpecification extends Specification<Loan> {
	/**
	 * Create a specification that matches loan with a given status.
	 *
	 * @param status the stauts to search for
	 * @return a specification that matches books with an loan containing the given string
	 */
	static @Nullable LoanSpecification statusLike(@Nullable String status) {
		if (status == null) {
			return null;
		} else {
			return (root, query, cb) -> cb.like(cb.lower(root.get("status")), "%" + status.toLowerCase() + "%");
		}
	}
}