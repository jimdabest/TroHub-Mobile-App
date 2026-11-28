package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.TenantRequest;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.PageResponse;
import me.ihqqq.trohub.dto.response.TenantInviteResponse;
import me.ihqqq.trohub.dto.response.TenantResponse;
import me.ihqqq.trohub.service.TenantLinkService;
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
    TenantLinkService tenantLinkService;

    @GetMapping
    ApiResponse<PageResponse<TenantResponse>> list(@PathVariable UUID propertyId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(tenantService.list(propertyId, page, size));
    }

    @PostMapping("/{tenantId}/invite")
    @Operation(summary = "Tạo mã mời để khách liên kết tài khoản vào tenant portal (mã chỉ hiện một lần, hạn 7 ngày)")
    ApiResponse<TenantInviteResponse> invite(@PathVariable UUID propertyId, @PathVariable UUID tenantId) {
        return ApiResponse.ok(tenantLinkService.invite(propertyId, tenantId));
    }

    @DeleteMapping("/{tenantId}/link")
    @Operation(summary = "Gỡ liên kết tài khoản khỏi hồ sơ người thuê và hủy mã mời đang chờ")
    ApiResponse<TenantResponse> unlink(@PathVariable UUID propertyId, @PathVariable UUID tenantId) {
        return ApiResponse.ok(tenantLinkService.unlink(propertyId, tenantId));
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