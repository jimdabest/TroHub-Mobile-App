package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceSummaryResponse {
    UUID id;
    UUID propertyId;
    UUID roomId;
    String roomCode;
    UUID contractId;
    LocalDate period;
    LocalDate issueDate;
    LocalDate dueDate;
    BigDecimal totalAmount;
    BigDecimal paidAmount;
    BigDecimal remainingAmount;
    InvoiceStatus status;
    String note;
    Instant createdAt;
    Instant updatedAt;
}