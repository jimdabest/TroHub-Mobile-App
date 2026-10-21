package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.PropertyRole;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PropertyResponse {
    UUID id;
    String name;
    String address;
    String description;
    PropertyRole myRole;
    Instant createdAt;
    Instant updatedAt;
}