package me.ihqqq.trohub.mapper;

import me.ihqqq.trohub.dto.request.ServicePriceRequest;
import me.ihqqq.trohub.dto.response.ServicePriceResponse;
import me.ihqqq.trohub.entity.ServicePrice;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServicePriceMapper {
    ServicePrice toServicePrice(ServicePriceRequest request);

    @Mapping(source = "property.id", target = "propertyId")
    ServicePriceResponse toResponse(ServicePrice servicePrice);

    void update(@MappingTarget ServicePrice servicePrice, ServicePriceRequest request);
}