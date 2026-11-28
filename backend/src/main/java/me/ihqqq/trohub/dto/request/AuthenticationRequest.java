package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    @NotBlank(message = "CREDENTIAL_REQUIRED")
    String username;
    @NotBlank(message = "CREDENTIAL_REQUIRED")
    String password;
}