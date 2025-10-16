package com.example.library.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.library.entity.Category;

/*
 * Repository for the Category entity.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

	/**
	 * Find a soft deleted category by its id.
	 *
	 * @param id the id of the category
	 * @return an optional with the category if found, empty otherwise
	 */
	Optional<Category> findByIdAndDeletedTrue(Long id);
}
