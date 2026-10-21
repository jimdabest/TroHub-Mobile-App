package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.TenantRequest;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.TenantResponse;
import me.ihqqq.trohub.service.TenantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}/tenants")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Tenants")
public class TenantController {

    TenantService tenantService;

    @GetMapping
    ApiResponse<List<TenantResponse>> list(@PathVariable UUID propertyId) {
        return ApiResponse.ok(tenantService.list(propertyId));
    }

    @GetMapping("/{tenantId}")
    ApiResponse<TenantResponse> get(@PathVariable UUID propertyId, @PathVariable UUID tenantId) {
        return ApiResponse.ok(tenantService.get(propertyId, tenantId));
    }

    @PostMapping
    ApiResponse<TenantResponse> create(@PathVariable UUID propertyId, @RequestBody @Valid TenantRequest request) {
        return ApiResponse.ok(tenantService.create(propertyId, request));
    }

    @PutMapping("/{tenantId}")
    ApiResponse<TenantResponse> update(@PathVariable UUID propertyId, @PathVariable UUID tenantId,
                                       @RequestBody @Valid TenantRequest request) {
        return ApiResponse.ok(tenantService.update(propertyId, tenantId, request));
    }

    @DeleteMapping("/{tenantId}")
    ApiResponse<Void> delete(@PathVariable UUID propertyId, @PathVariable UUID tenantId) {
        tenantService.delete(propertyId, tenantId);
        return ApiResponse.ok("Đã xóa người thuê");
    }
}