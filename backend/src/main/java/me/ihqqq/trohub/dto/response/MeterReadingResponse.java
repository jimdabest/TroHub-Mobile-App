package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.ServiceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MeterReadingResponse {
    UUID id;
    UUID propertyId;
    UUID roomId;
    String roomCode;
    UUID servicePriceId;
    String serviceName;
    ServiceType serviceType;
    String unit;
    LocalDate period;
    BigDecimal previousValue;
    BigDecimal currentValue;
    BigDecimal consumption;
    Instant readAt;
    Instant createdAt;
    Instant updatedAt;
}