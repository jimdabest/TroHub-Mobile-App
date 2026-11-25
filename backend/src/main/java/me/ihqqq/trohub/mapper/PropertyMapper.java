package me.ihqqq.trohub.mapper;

import me.ihqqq.trohub.dto.request.PropertyRequest;
import me.ihqqq.trohub.dto.response.PropertyResponse;
import me.ihqqq.trohub.entity.Property;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PropertyMapper {
    @Mapping(target = "bankName", ignore = true)
    @Mapping(target = "bankAccountNumber", ignore = true)
    @Mapping(target = "bankAccountHolder", ignore = true)
    Property toProperty(PropertyRequest request);

    PropertyResponse toResponse(Property property);

    @Mapping(target = "bankName", ignore = true)
    @Mapping(target = "bankAccountNumber", ignore = true)
    @Mapping(target = "bankAccountHolder", ignore = true)
    void update(@MappingTarget Property property, PropertyRequest request);
}