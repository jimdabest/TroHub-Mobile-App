package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.MeterReadingRequest;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.MeterReadingResponse;
import me.ihqqq.trohub.service.MeterReadingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Meter readings")
public class MeterReadingController {

    MeterReadingService meterReadingService;

    @PostMapping("/meter-readings")
    ApiResponse<MeterReadingResponse> create(@PathVariable UUID propertyId,
                                             @RequestBody @Valid MeterReadingRequest request) {
        return ApiResponse.ok(meterReadingService.create(propertyId, request));
    }

    @GetMapping("/rooms/{roomId}/meter-readings/latest")
    ApiResponse<List<MeterReadingResponse>> latest(@PathVariable UUID propertyId, @PathVariable UUID roomId) {
        return ApiResponse.ok(meterReadingService.latest(propertyId, roomId));
    }

    @GetMapping("/meter-readings")
    ApiResponse<List<MeterReadingResponse>> listByPeriod(
            @PathVariable UUID propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period) {
        return ApiResponse.ok(meterReadingService.listByPeriod(propertyId, period));
    }
}