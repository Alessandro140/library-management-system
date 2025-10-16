package com.example.library.mapper;

import com.example.library.dto.UserDTO;
import com.example.library.entity.User;
import com.example.library.repository.LoanRepository;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    LoanRepository loanRepository;

    @Autowired
    LoanMapper loanMapper;

    @IgnoreAuditFields
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "loans", ignore = true)
    public abstract User toEntity(UserDTO dto);

    public abstract UserDTO toDto(User entity);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "loans", ignore = true)
    public abstract User updateUser(UserDTO dto, @MappingTarget User entity);

}
