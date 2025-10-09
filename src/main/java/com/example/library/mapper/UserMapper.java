package com.example.library.mapper;

import com.example.library.dto.UserDTO;
import com.example.library.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {LoanMapper.class})
public interface UserMapper {

    @IgnoreAuditFields
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    User toEntity(UserDTO dto);

    UserDTO toDto(User entity);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    User updateUser(UserDTO dto, @MappingTarget User entity);
}
