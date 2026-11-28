package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceTierRequest {
    BigDecimal upTo;
    @NotNull(message = "SERVICE_PRICE_INVALID")
    @DecimalMin(value = "0", message = "SERVICE_PRICE_INVALID")
    BigDecimal unitPrice;
}