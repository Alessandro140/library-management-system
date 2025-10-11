package com.example.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.example.library.entity.User;

/*
 * Repository for the User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmailAndDeletedFalse(String email);

    /**
     *
	 * Find an user by its id.
	 *
	 * @param id the id of the user
	 * @return an optional with the user if found, empty otherwise
	 */
	Optional<User> findByIdAndDeletedFalse(Long id);

	/**
	 * Find a soft deleted user by its id.
	 *
	 * @param id the id of the user
	 * @return an optional with the user if found, empty otherwise
	 */
	Optional<User> findByIdAndDeletedTrue(Long id);
}
