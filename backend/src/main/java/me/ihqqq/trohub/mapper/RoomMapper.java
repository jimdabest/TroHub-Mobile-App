package me.ihqqq.trohub.mapper;

import me.ihqqq.trohub.dto.request.RoomRequest;
import me.ihqqq.trohub.dto.response.RoomResponse;
import me.ihqqq.trohub.entity.Room;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoomMapper {
    Room toRoom(RoomRequest request);

    @Mapping(source = "property.id", target = "propertyId")
    RoomResponse toResponse(Room room);

    void update(@MappingTarget Room room, RoomRequest request);
}