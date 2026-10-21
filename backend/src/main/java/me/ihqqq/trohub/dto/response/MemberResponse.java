package me.ihqqq.trohub.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.PropertyRole;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MemberResponse {
    UUID userId;
    String username;
    String fullName;
    PropertyRole role;
}