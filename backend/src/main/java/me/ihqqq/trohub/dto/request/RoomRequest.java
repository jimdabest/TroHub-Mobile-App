package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomRequest {
    @NotBlank(message = "ROOM_CODE_REQUIRED")
    String code;
    String block;
    Integer floor;
    BigDecimal areaM2;
    @NotNull(message = "ROOM_PRICE_INVALID")
    @DecimalMin(value = "0", message = "ROOM_PRICE_INVALID")
    BigDecimal price;
    String description;
}