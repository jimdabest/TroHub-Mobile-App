package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.ContractStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractResponse {
    UUID id;
    UUID propertyId;
    UUID roomId;
    String roomCode;
    UUID tenantId;
    String tenantName;
    LocalDate startDate;
    LocalDate endDate;
    BigDecimal monthlyRent;
    BigDecimal deposit;
    ContractStatus status;
    String note;
    Instant createdAt;
    Instant updatedAt;
}