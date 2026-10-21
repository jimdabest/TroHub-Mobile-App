package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.ServiceType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServicePriceRequest {
    @NotBlank(message = "SERVICE_NAME_REQUIRED")
    String name;
    @NotNull(message = "SERVICE_TYPE_REQUIRED")
    ServiceType type;
    @NotBlank(message = "SERVICE_UNIT_REQUIRED")
    String unit;
    @NotNull(message = "SERVICE_PRICE_INVALID")
    @DecimalMin(value = "0", message = "SERVICE_PRICE_INVALID")
    BigDecimal unitPrice;
    Boolean active;
}