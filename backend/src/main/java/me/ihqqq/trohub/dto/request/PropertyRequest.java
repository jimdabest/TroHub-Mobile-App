package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PropertyRequest {
    @NotBlank(message = "PROPERTY_NAME_REQUIRED")
    String name;
    String address;
    String description;

    @Size(max = 100, message = "INVALID_REQUEST")
    String bankName;
    @Size(max = 30, message = "INVALID_REQUEST")
    String bankAccountNumber;
    @Size(max = 100, message = "INVALID_REQUEST")
    String bankAccountHolder;
}