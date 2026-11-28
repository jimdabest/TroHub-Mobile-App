package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.ServicePriceRequest;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.ServicePriceResponse;
import me.ihqqq.trohub.service.ServicePriceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}/service-prices")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Service prices")
public class ServicePriceController {

    ServicePriceService servicePriceService;

    @GetMapping
    ApiResponse<List<ServicePriceResponse>> list(@PathVariable UUID propertyId) {
        return ApiResponse.ok(servicePriceService.list(propertyId));
    }

    @PostMapping
    ApiResponse<ServicePriceResponse> create(@PathVariable UUID propertyId,
                                             @RequestBody @Valid ServicePriceRequest request) {
        return ApiResponse.ok(servicePriceService.create(propertyId, request));
    }

    @PutMapping("/{serviceId}")
    ApiResponse<ServicePriceResponse> update(@PathVariable UUID propertyId, @PathVariable UUID serviceId,
                                             @RequestBody @Valid ServicePriceRequest request) {
        return ApiResponse.ok(servicePriceService.update(propertyId, serviceId, request));
    }

    @DeleteMapping("/{serviceId}")
    ApiResponse<Void> delete(@PathVariable UUID propertyId, @PathVariable UUID serviceId) {
        servicePriceService.delete(propertyId, serviceId);
        return ApiResponse.ok("Đã xóa dịch vụ");
    }
}