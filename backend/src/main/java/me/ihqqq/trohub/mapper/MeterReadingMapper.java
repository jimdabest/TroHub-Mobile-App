package me.ihqqq.trohub.mapper;

import me.ihqqq.trohub.dto.response.MeterReadingResponse;
import me.ihqqq.trohub.entity.MeterReading;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MeterReadingMapper {

    @Mapping(source = "property.id", target = "propertyId")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.code", target = "roomCode")
    @Mapping(source = "servicePrice.id", target = "servicePriceId")
    @Mapping(source = "servicePrice.name", target = "serviceName")
    @Mapping(source = "servicePrice.type", target = "serviceType")
    @Mapping(source = "servicePrice.unit", target = "unit")
    @Mapping(target = "consumption", expression = "java(reading.getCurrentValue().subtract(reading.getPreviousValue()))")
    MeterReadingResponse toResponse(MeterReading reading);
}