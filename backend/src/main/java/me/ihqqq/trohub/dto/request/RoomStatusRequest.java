package me.ihqqq.trohub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.RoomStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomStatusRequest {
    @NotNull(message = "STATUS_REQUIRED")
    RoomStatus status;
}