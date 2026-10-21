package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractCreationRequest {
    @NotNull(message = "CONTRACT_ROOM_REQUIRED")
    UUID roomId;
    @NotNull(message = "CONTRACT_TENANT_REQUIRED")
    UUID tenantId;
    @NotNull(message = "CONTRACT_START_REQUIRED")
    LocalDate startDate;
    LocalDate endDate;
    @DecimalMin(value = "0", message = "CONTRACT_AMOUNT_INVALID")
    BigDecimal monthlyRent;
    @DecimalMin(value = "0", message = "CONTRACT_AMOUNT_INVALID")
    BigDecimal deposit;
    String note;
}