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
public class MeterReadingRequest {
    @NotNull(message = "METER_ROOM_REQUIRED")
    UUID roomId;

    @NotNull(message = "METER_SERVICE_REQUIRED")
    UUID servicePriceId;

    @NotNull(message = "METER_PERIOD_REQUIRED")
    LocalDate period;

    @DecimalMin(value = "0", message = "METER_VALUE_INVALID")
    BigDecimal previousValue;

    @NotNull(message = "METER_CURRENT_REQUIRED")
    @DecimalMin(value = "0", message = "METER_VALUE_INVALID")
    BigDecimal currentValue;
}