package com.example.library.mapper;

import com.example.library.dto.LoanDTO;
import com.example.library.entity.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {BookMapper.class, UserMapper.class})
public interface LoanMapper {

    @IgnoreAuditFields
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "user", ignore = true)
    Loan toEntity(LoanDTO dto);

    @Mapping(target = "userId", source = "user.id")
    LoanDTO toDto(Loan entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "user", ignore = true)
    @IgnoreAuditFields
    Loan updateLoan(LoanDTO dto, @MappingTarget Loan entity);

}
