package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.RoomStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomResponse {
    UUID id;
    UUID propertyId;
    String code;
    String block;
    Integer floor;
    BigDecimal areaM2;
    BigDecimal price;
    RoomStatus status;
    String description;
    Instant createdAt;
    Instant updatedAt;
}