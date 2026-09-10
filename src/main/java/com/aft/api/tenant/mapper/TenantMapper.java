package com.aft.api.tenant.mapper;

import com.aft.api.tenant.dto.OrganizationDto;
import com.aft.api.tenant.entity.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TenantMapper {
    @Mapping(target = "status", expression = "java(organization.getStatus().name())")
    OrganizationDto toDto(Organization organization);
}
