package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.NotBlank;
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
}