package me.ihqqq.trohub.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.Role;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    UUID id;
    String username;
    String fullName;
    String phone;
    String email;
    Role role;
}