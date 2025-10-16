package com.example.library.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.library.entity.Author;

/*
 * Repository for the Author entity.
 */
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long>, JpaSpecificationExecutor<Author> {

	/**
	 * Find a soft deleted author by its id.
	 *
	 * @param id the id of the author
	 * @return an optional with the author if found, empty otherwise
	 */
	Optional<Author> findByIdAndDeletedTrue(Long id);
}
