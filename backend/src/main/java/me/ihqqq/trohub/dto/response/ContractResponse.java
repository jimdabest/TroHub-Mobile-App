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
    int occupants;
    boolean depositSettled;       // đã quyết toán tiền cọc
    BigDecimal depositRefunded;   // đã hoàn lại cho khách
    BigDecimal depositDeducted;   // giữ lại (nợ, hư hỏng...)
    String depositNote;
    ContractStatus status;
    boolean expired;         // ACTIVE nhưng đã qua ngày kết thúc dự kiến (khách có thể vẫn đang ở, cần gia hạn hoặc kết thúc)
    Long daysUntilEnd;       // số ngày đến ngày kết thúc dự kiến, âm nếu đã qua; null nếu không có ngày kết thúc hoặc đã kết thúc
    String note;
    Instant createdAt;
    Instant updatedAt;
}