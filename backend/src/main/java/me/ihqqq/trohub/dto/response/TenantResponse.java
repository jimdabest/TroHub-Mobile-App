package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TenantResponse {
    UUID id;
    UUID propertyId;
    String fullName;
    String phone;
    String idNumber;
    String note;
    Instant createdAt;
    Instant updatedAt;
}