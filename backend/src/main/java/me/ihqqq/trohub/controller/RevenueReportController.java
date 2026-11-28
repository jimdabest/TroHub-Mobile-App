package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.RevenueReportResponse;
import me.ihqqq.trohub.service.RevenueReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}/reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Reports")
public class RevenueReportController {

    RevenueReportService revenueReportService;

    @GetMapping("/revenue")
    @Operation(summary = "Báo cáo doanh thu theo tháng (mặc định 6 tháng gần nhất, tối đa 24 tháng; chỉ OWNER)")
    ApiResponse<RevenueReportResponse> revenue(
            @PathVariable UUID propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.ok(revenueReportService.revenue(propertyId, from, to));
    }
}