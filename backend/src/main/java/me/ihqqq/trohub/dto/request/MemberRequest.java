package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.PropertyRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MemberRequest {
    @NotBlank(message = "USERNAME_REQUIRED")
    String username;
    @NotNull(message = "ROLE_REQUIRED")
    PropertyRole role;
}