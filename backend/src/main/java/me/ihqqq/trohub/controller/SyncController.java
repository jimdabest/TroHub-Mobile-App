package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.SyncResponse;
import me.ihqqq.trohub.service.SyncService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}/sync")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Sync")
public class SyncController {

    SyncService syncService;

    @GetMapping
    @Operation(summary = "Kéo thay đổi kể từ `since` (ISO-8601, vd 2026-11-26T08:34:22Z); bỏ trống = kéo toàn bộ. "
            + "Lưu `syncedAt` trả về để làm `since` lần sau.")
    ApiResponse<SyncResponse> pull(@PathVariable UUID propertyId,
                                   @RequestParam(required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
        return ApiResponse.ok(syncService.pull(propertyId, since));
    }
}