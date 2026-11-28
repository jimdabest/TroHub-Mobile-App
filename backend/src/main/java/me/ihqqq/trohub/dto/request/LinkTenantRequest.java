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
public class LinkTenantRequest {
    @NotBlank(message = "INVITE_CODE_INVALID")
    @Size(max = 20, message = "INVITE_CODE_INVALID")
    String code;
}