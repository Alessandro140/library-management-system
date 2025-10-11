package com.example.library.service;

import com.example.library.dto.LoanDTO;
import com.example.library.entity.Loan;
import com.example.library.entity.User;
import com.example.library.utils.RepositoryException;
import com.example.library.mapper.LoanMapper;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class LoanService {

    private final @NonNull LoanRepository loanRepository;
    private final @NonNull UserRepository userRepository;
    private final @NonNull LoanMapper loanMapper;

    public LoanService(@NonNull LoanRepository loanRepository, @NonNull LoanMapper loanMapper, @NonNull UserRepository userRepository){
        this.loanMapper = loanMapper;
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    /**
     * Return an optional with inside a loanDTO or empty.
     *
     * @param id
     * @param pageable
     * @return an optional with the dto of the loan
     */
    public @NonNull Optional<LoanDTO> getLoanById(@NonNull Long id, @NonNull Pageable pageable){
        return this.loanRepository.findByIdAndDeletedFalse(id)
                .map(this.loanMapper::toDto);
    }


    public @NonNull Page<LoanDTO> getLoans(Specification<Loan> loanSpecification, @NonNull Pageable pageable){
        Specification<Loan> notDeletedSpec = (root, query, criteriaBuilder) ->
            criteriaBuilder.isFalse(root.get("deleted"));

        if (loanSpecification != null) {
            loanSpecification = loanSpecification.and(notDeletedSpec);
        } else {
            loanSpecification = notDeletedSpec;
        }

        return loanRepository.findAll(loanSpecification, pageable).map(this.loanMapper::toDto);

    }

    /**
     * Create a new loan.
     *
     * @param loan
     * @return the saved loan dto
     */
    @Transactional
    public @NonNull LoanDTO createLoan(@NonNull LoanDTO loanDTO) throws UserNotFoundException{

        loanDTO.setId(null);

        Loan loanToSave = this.loanMapper.toEntity(loanDTO);
        User user = this.userRepository.findByIdAndDeletedFalse(loanDTO.getUser_id())
                .orElseThrow(() -> new UserNotFoundException(loanDTO.getUser_id()));
        loanToSave.setUser(user);
        Loan savedLoan = this.loanRepository.save(loanToSave);

        return this.loanMapper.toDto(savedLoan);
    }

    /**
     * Update and loan.
     *
     * @param id
     * @param loanDTO
     * @return the DTO of the modified loan
     * @throws LoanNotFoundException
     */
    @Transactional
    public @NonNull LoanDTO updateLoan(@NonNull Long id, @NonNull LoanDTO loanDTO) throws LoanNotFoundException, UserNotFoundException{

        Loan loanToModify = this.loanRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        this.loanMapper.updateLoan(loanDTO, loanToModify);

        User user = this.userRepository.findByIdAndDeletedFalse(loanDTO.getUser_id())
                .orElseThrow(() -> new UserNotFoundException(loanDTO.getUser_id()));
        loanToModify.setUser(user);

        return this.loanMapper.toDto(loanToModify);
    }

    /**
     * Soft delete an loan.
     *
     * @param id
     * @throws LoanNotFoundException
     */
    @Transactional
    public void softDeleteLoan(@NonNull Long id) throws LoanNotFoundException{
        Loan loan = this.loanRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new LoanNotFoundException(id));
        loan.setDeleted(true);
    }

    /**
     * Delete an loan
     *
     * @param id
     * @throws LoanNotFoundException
     */
    @Transactional
    public void deleteLoan(@NonNull Long id) throws LoanNotFoundException{
        if (!this.loanRepository.existsById(id)) {
            throw new LoanNotFoundException(id);
        }
        this.loanRepository.deleteById(id);
    }


    /**
     * Restore a soft deleted loan by its id.
     *
     * @param id
     * @return
     * @throws LoanNotFoundException
     */
    @Transactional
    public LoanDTO restoreLoanById(@NonNull Long id) throws LoanNotFoundException{
        Loan loanToRestore = this.loanRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        loanToRestore.setDeleted(false);

        return this.loanMapper.toDto(loanToRestore);
    }

    /**
     * Exception thrown when a loan doesn't exist.
     */
    public static class LoanNotFoundException extends RepositoryException.NotFound {
        /**
         * Creates a new LoanNotFoundException with the given id.
         *
         * @param id - the id of the loan
         */
        public LoanNotFoundException(@NotNull Long id) {
            super("Loan doesn't exist with id: " + id);
        }
    }

    /**
     * Exception thrown when a user doesn't exists.
     */
    public static class UserNotFoundException extends RepositoryException.NotFound {
        /**
         * Creates a new UserNotFoundException with the given id.
         *
         * @param id - the id of the user
         */
        public UserNotFoundException(@NotNull Long id) {
            super("User doesn't exist with id: " + id);
        }
    }

}
