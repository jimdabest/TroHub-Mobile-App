package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.InvoiceItemType;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceItemResponse {
    UUID id;
    InvoiceItemType itemType;
    String description;
    BigDecimal quantity;
    BigDecimal unitPrice;
    BigDecimal amount;
}