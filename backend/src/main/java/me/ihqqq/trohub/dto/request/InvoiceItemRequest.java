package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceItemRequest {
    @NotBlank(message = "INVOICE_ITEM_DESCRIPTION_REQUIRED")
    @Size(max = 200, message = "INVOICE_ITEM_DESCRIPTION_REQUIRED")
    String description;

    @DecimalMin(value = "0", inclusive = false, message = "INVOICE_ITEM_QUANTITY_INVALID")
    @Digits(integer = 9, fraction = 2, message = "INVOICE_ITEM_QUANTITY_INVALID")
    BigDecimal quantity;   // mặc định 1

    @NotNull(message = "INVOICE_ITEM_PRICE_INVALID")
    @Digits(integer = 11, fraction = 2, message = "INVOICE_ITEM_PRICE_INVALID")
    BigDecimal unitPrice;
}