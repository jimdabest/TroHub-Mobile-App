package me.ihqqq.trohub.mapper;

import me.ihqqq.trohub.dto.request.TenantRequest;
import me.ihqqq.trohub.dto.response.TenantResponse;
import me.ihqqq.trohub.entity.Tenant;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantMapper {
    Tenant toTenant(TenantRequest request);

    @Mapping(source = "property.id", target = "propertyId")
    TenantResponse toResponse(Tenant tenant);

    void update(@MappingTarget Tenant tenant, TenantRequest request);
}