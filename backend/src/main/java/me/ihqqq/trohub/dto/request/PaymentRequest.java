package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.PaymentMethod;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequest {
    @NotNull(message = "PAYMENT_AMOUNT_INVALID")
    @DecimalMin(value = "0", inclusive = false, message = "PAYMENT_AMOUNT_INVALID")
    BigDecimal amount;

    @NotNull(message = "PAYMENT_METHOD_REQUIRED")
    PaymentMethod method;

    @Size(max = 500, message = "INVALID_REQUEST")
    String note;
}