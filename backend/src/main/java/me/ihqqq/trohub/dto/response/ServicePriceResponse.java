package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.ServiceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServicePriceResponse {
    UUID id;
    UUID propertyId;
    String name;
    ServiceType type;
    String unit;
    BigDecimal unitPrice;
    boolean active;
    Instant createdAt;
    Instant updatedAt;
}