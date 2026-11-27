package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/** Quyết toán tiền cọc: hoàn lại + giữ lại không được vượt tiền cọc của hợp đồng. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepositSettlementRequest {
    @NotNull(message = "DEPOSIT_SETTLEMENT_INVALID")
    @DecimalMin(value = "0", message = "DEPOSIT_SETTLEMENT_INVALID")
    @Digits(integer = 12, fraction = 2, message = "DEPOSIT_SETTLEMENT_INVALID")
    BigDecimal depositRefunded;

    @NotNull(message = "DEPOSIT_SETTLEMENT_INVALID")
    @DecimalMin(value = "0", message = "DEPOSIT_SETTLEMENT_INVALID")
    @Digits(integer = 12, fraction = 2, message = "DEPOSIT_SETTLEMENT_INVALID")
    BigDecimal depositDeducted;

    @Size(max = 500, message = "DEPOSIT_SETTLEMENT_INVALID")
    String depositNote;
}