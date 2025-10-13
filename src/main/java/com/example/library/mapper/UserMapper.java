package com.example.library.mapper;

import com.example.library.dto.LoanDTO;
import com.example.library.dto.UserDTO;
import com.example.library.entity.Loan;
import com.example.library.entity.User;
import com.example.library.repository.LoanRepository;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {LoanMapper.class})
public abstract class UserMapper {

    @Autowired
    LoanRepository loanRepository;

    @Autowired
    LoanMapper loanMapper;

    @IgnoreAuditFields
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    public abstract User toEntity(UserDTO dto);

    public abstract UserDTO toDto(User entity);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    public abstract User updateUser(UserDTO dto, @MappingTarget User entity);

    @AfterMapping
    protected void mergeLoans(UserDTO dto, @MappingTarget User entity) {
        // rimuove l'utente dai loan che non sono più nella lista del DTO
        entity.getLoans().forEach(loan -> {
            if (dto.getLoans() == null || dto.getLoans().stream().noneMatch(l -> l.getId().equals(loan.getId()))) {
                loan.setUser(null);
            }
        });

        entity.getLoans().clear();

        if (dto.getLoans() == null) {
            return;
        }

        for (LoanDTO loanDto : dto.getLoans()) {
            Loan loan = loanRepository.findById(loanDto.getId())
                    .orElseThrow(() -> new RuntimeException("Loan not found: " + loanDto.getId()));

            loanMapper.updateLoan(loanDto, loan);

            // aggiorna lato proprietario
            loan.setUser(entity);

            entity.getLoans().add(loan);
        }
    }
}
