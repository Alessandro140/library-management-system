package com.example.library.specification;

import com.example.library.entity.Book;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

/**
 * Defines specifications for querying books.
 */
public interface BookSpecification extends Specification<Book> {
	/**
	 * Create a specification that matches books with a title containing the given string.
	 *
	 * @param title the title to search for
	 * @return a specification that matches books with a title containing the given string
	 */
	static @Nullable BookSpecification titleLike(@Nullable String title) {
		if (title == null) {
			return null;
		} else {
			return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
		}
	}

	/**
	 * Create a specification that matches books with an author containing the given string.
	 *
	 * @param author the author to search for
	 * @return a specification that matches books with an author containing the given string
	 */
	static @Nullable BookSpecification authorLike(@Nullable String author) {
		if (author == null) {
			return null;
		} else {
			return (root, query, cb) -> cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%");
		}
	}
}