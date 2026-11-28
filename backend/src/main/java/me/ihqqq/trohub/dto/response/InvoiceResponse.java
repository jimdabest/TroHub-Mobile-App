package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceResponse {
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
    boolean overdue;        // chưa trả đủ và đã qua hạn thanh toán
    long daysOverdue;       // số ngày quá hạn, 0 nếu chưa quá hạn
    String note;
    List<InvoiceItemResponse> items;
    Instant createdAt;
    Instant updatedAt;
}