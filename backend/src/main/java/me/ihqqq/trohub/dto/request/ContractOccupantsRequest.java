package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractOccupantsRequest {
    @NotNull(message = "CONTRACT_OCCUPANTS_INVALID")
    @Min(value = 1, message = "CONTRACT_OCCUPANTS_INVALID")
    Integer occupants;
}