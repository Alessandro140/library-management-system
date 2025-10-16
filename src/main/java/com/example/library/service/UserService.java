package com.example.library.service;

import com.example.library.dto.UserDTO;
import com.example.library.entity.User;
import com.example.library.utils.RepositoryException;
import com.example.library.mapper.UserMapper;
import com.example.library.repository.UserRepository;
import com.example.library.specification.SpecsNotDeleted;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

/**
 * Service implementation of the user entity.
 */
@Service
public class UserService {

    private final @NonNull UserRepository userRepository;
    private final @NonNull UserMapper userMapper;

    public UserService(@NonNull UserRepository userRepository, @NonNull UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Get a single user by its id.
     *
     * @param id the id of the user
     * @return an optional with the user if found, empty otherwise
     */
    public @NonNull Optional<UserDTO> getUserById(@NonNull Long id) throws NullInputException {

        if(id == null){
            throw new NullInputException("id");
        }

        // Find the user by its ID and map it to a DTO.
        return this.userRepository.findByIdAndDeletedFalse(id).map(this.userMapper::toDto);
    }

    /**
     * Get all the users.
     *
     * @param userSpecification
     * @param pageable
     * @return A page with all the users
     */
    public @NonNull Page<UserDTO> getUsers(Specification<User> userSpecification, @NonNull Pageable pageable) throws NullInputException{

        if(pageable == null){
            throw new NullInputException("pageable");
        }

        Specification<User> specUser = SpecsNotDeleted.ensureNotDeleted(userSpecification);

        return userRepository.findAll(specUser, pageable).map(this.userMapper::toDto);
    }

    /**
     * Create a new user.
     *
     * @param user
     * @return the saved user dto
     */
    @Transactional
    public @NonNull UserDTO createUser(@NonNull UserDTO userDTO) throws UserAlreadyExistsException, NullInputException {
        if(userDTO == null){
            throw new NullInputException("userDTO");
        }

        if(this.userRepository.findByEmailAndDeletedFalse(userDTO.getEmail()).isPresent()){
            throw new UserAlreadyExistsException(userDTO.getEmail());
        }
        userDTO.setId(null);

        User userToSave = this.userMapper.toEntity(userDTO);
        User savedUser = this.userRepository.save(userToSave);

        return this.userMapper.toDto(savedUser);
    }

    /**
     * Update and user.
     *
     * @param id
     * @param userDTO
     * @return the DTO of the modified user
     * @throws UserNotFoundException
     */
    @Transactional
    public @NonNull UserDTO updateUser(@NonNull Long id, @NonNull UserDTO userDTO) throws UserNotFoundException, UserAlreadyExistsException, NullInputException{

        if(id == null && userDTO == null){
            throw new NullInputException("id", "userDTO");
        } else if (id == null){
            throw new NullInputException("id");
        } else if (userDTO == null){
            throw new NullInputException("userDTO");
        }

        User userToModify = this.userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        Optional<User> other = this.userRepository.findByEmailAndDeletedFalse(userDTO.getEmail());

        if (other.isPresent() && !other.get().getId().equals(id)) {
            throw new UserAlreadyExistsException(userDTO.getEmail());
        }

        this.userMapper.updateUser(userDTO, userToModify);

        return this.userMapper.toDto(userToModify);
    }

    /**
     * Soft delete an user.
     *
     * @param id
     * @throws UserNotFoundException
     */
    @Transactional
    public void softDeleteUser(@NonNull Long id) throws UserNotFoundException, NullInputException{
        if(id == null){
            throw new NullInputException("id");
        }
        User user = this.userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setDeleted(true);
    }

    /**
     * Delete an user
     *
     * @param id
     * @throws UserNotFoundException
     */
    @Transactional
    public void deleteUser(@NonNull Long id) throws UserNotFoundException, NullInputException{
        if(id == null){
            throw new NullInputException("id");
        }
        if (!this.userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        this.userRepository.deleteById(id);
    }


    /**
     * Restore a soft deleted user by its id.
     *
     * @param id
     * @return
     * @throws UserNotFoundException
     */
    @Transactional
    public UserDTO restoreUserById(@NonNull Long id) throws UserNotFoundException, NullInputException{
        if(id == null){
            throw new NullInputException("id");
        }

        User userToRestore = this.userRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userToRestore.setDeleted(false);

        return this.userMapper.toDto(userToRestore);
    }

    /**
     * Exception thrown when a user already exists.
     */
    public static class UserNotFoundException extends RepositoryException.NotFound {
        /**
         * Creates a new UserAlreadyExistsException with the given id.
         *
         * @param id - the id of the user
         */
        public UserNotFoundException(@NotNull Long id) {
            super("User doesn't exist with id: " + id);
        }
    }

    /**
     * Exception thrown when a book already exists.
     */
    public static class UserAlreadyExistsException extends RepositoryException.Conflict {
        /**
         * Creates a new UserAlreadyExistsException with the given email.
         *
         * @param email - the email of the book
         */
        public UserAlreadyExistsException(@NotNull String email) {
            super("User already exists with email: " + email);
        }
    }

    public static class NullInputException extends RepositoryException.BadRequest{

        public NullInputException(@NotNull String parameterName){
            super("This parameter should be NonNull: " + parameterName);
        }

        public NullInputException(@NotNull String parameterName1, @NotNull String parameterName2){
            super("This parameters should be NonNull: " + parameterName1 + ", " + parameterName2);
        }
    }
}
