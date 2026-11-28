package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceGenerationRequest {
    @NotNull(message = "INVOICE_PERIOD_REQUIRED")
    LocalDate period;

    List<UUID> roomIds;

    LocalDate dueDate;
}