package me.ihqqq.trohub.mapper;

import me.ihqqq.trohub.dto.response.ContractResponse;
import me.ihqqq.trohub.entity.Contract;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContractMapper {
    @Mapping(source = "property.id", target = "propertyId")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.code", target = "roomCode")
    @Mapping(source = "tenant.id", target = "tenantId")
    @Mapping(source = "tenant.fullName", target = "tenantName")
    ContractResponse toResponse(Contract contract);
}