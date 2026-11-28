package me.ihqqq.trohub.mapper;

import me.ihqqq.trohub.dto.request.ServicePriceRequest;
import me.ihqqq.trohub.dto.response.ServicePriceResponse;
import me.ihqqq.trohub.entity.ServicePrice;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServicePriceMapper {
    @Mapping(target = "tiers", ignore = true)   // validate và gán thủ công trong ServicePriceService
    ServicePrice toServicePrice(ServicePriceRequest request);

    @Mapping(source = "property.id", target = "propertyId")
    ServicePriceResponse toResponse(ServicePrice servicePrice);

    @Mapping(target = "tiers", ignore = true)
    void update(@MappingTarget ServicePrice servicePrice, ServicePriceRequest request);
}