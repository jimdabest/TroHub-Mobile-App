package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotNull(message = "USERNAME_INVALID")
    @Size(min = 4, max = 50, message = "USERNAME_INVALID")
    String username;
    @NotNull(message = "INVALID_PASSWORD")
    @Size(min = 8, max = 72, message = "INVALID_PASSWORD")
    String password;
    @NotBlank(message = "FULLNAME_REQUIRED")
    String fullName;
    String phone;
    @Email(message = "EMAIL_INVALID")
    String email;
}