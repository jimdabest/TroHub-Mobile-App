package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TenantRequest {
    @NotBlank(message = "TENANT_NAME_REQUIRED")
    String fullName;
    String phone;
    String idNumber;
    String note;
}