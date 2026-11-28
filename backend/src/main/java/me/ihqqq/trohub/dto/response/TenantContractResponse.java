package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.ContractStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TenantContractResponse {
    UUID contractId;
    ContractStatus status;
    LocalDate startDate;
    LocalDate endDate;
    BigDecimal monthlyRent;
    BigDecimal deposit;
    UUID roomId;
    String roomCode;
    UUID propertyId;
    String propertyName;
    String propertyAddress;
    String bankName;
    String bankAccountNumber;
    String bankAccountHolder;
}