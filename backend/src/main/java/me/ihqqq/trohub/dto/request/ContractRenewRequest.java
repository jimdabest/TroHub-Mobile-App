package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractRenewRequest {
    @NotNull(message = "CONTRACT_RENEW_DATE_INVALID")
    LocalDate newEndDate;
    /** Tiền thuê mới (tùy chọn, chỉ OWNER). Áp dụng cho hóa đơn tạo từ lúc này. */
    @DecimalMin(value = "0", message = "CONTRACT_AMOUNT_INVALID")
    BigDecimal monthlyRent;
}