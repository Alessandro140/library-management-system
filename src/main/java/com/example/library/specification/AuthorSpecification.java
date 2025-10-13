package com.example.library.specification;

import com.example.library.entity.Author;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

/**
 * Defines specifications for querying authors.
 */
public interface AuthorSpecification extends Specification<Author> {
	/**
	 * Create a specification that matches author with a given surname.
	 *
	 * @param author the author to search for
	 * @return a specification that matches books with an author containing the given string
	 */
	static @Nullable AuthorSpecification surnameLike(@Nullable String author) {
		if (author == null) {
			return null;
		} else {
			return (root, query, cb) -> cb.like(cb.lower(root.get("lastName")), "%" + author.toLowerCase() + "%");
		}
	}
}
