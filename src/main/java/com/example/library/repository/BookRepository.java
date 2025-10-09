package com.example.library.repository;

import com.example.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 * Repository for the Book entity.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
	/**
	 * Find a book by its ISBN.
	 *
	 * @param isbn the ISBN of the book
	 * @return an optional with the book if found, empty otherwise
	 */
	Optional<Book> findByISBNAndDeletedFalse(String isbn);


	/**
	 * Find a book by its id.
	 *
	 * @param id the id of the book
	 * @return an optional with the book if found, empty otherwise
	 */
	Optional<Book> findByIdAndDeletedFalse(Long id);

	/**
	 * Find a soft deleted book by its ISBN.
	 *
	 * @param isbn the ISBN of the book
	 * @return an optional with the book if found, empty otherwise
	 */
	Optional<Book> findByISBNAndDeletedTrue(String isbn);


	/**
	 * Find a soft deleted book by its id.
	 *
	 * @param id the id of the book
	 * @return an optional with the book if found, empty otherwise
	 */
	Optional<Book> findByIdAndDeletedTrue(Long id);
}