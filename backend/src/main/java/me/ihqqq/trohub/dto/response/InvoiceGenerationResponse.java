package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceGenerationResponse {
    List<InvoiceSummaryResponse> created;
    List<SkippedRoom> skipped;

    public enum SkipReason {
        ALREADY_INVOICED, NO_ACTIVE_CONTRACT, MISSING_METER_READING
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SkippedRoom {
        UUID roomId;
        String roomCode;
        SkipReason reason;
        String detail;
    }
}