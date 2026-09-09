package com.aft.api.user.mapper;

import com.aft.api.user.dto.UserDto;
import com.aft.api.user.entity.Role;
import com.aft.api.user.entity.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "roleCodes")
    UserDto toDto(UserAccount user);

    @Named("roleCodes")
    default Set<String> roleCodes(Set<Role> roles) {
        return roles.stream().map(role -> role.getCode().name()).collect(Collectors.toSet());
    }
}
