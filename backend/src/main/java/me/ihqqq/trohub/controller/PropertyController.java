package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.MemberRequest;
import me.ihqqq.trohub.dto.request.PropertyRequest;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.MemberResponse;
import me.ihqqq.trohub.dto.response.PropertyResponse;
import me.ihqqq.trohub.service.PropertyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Properties")
public class PropertyController {

    PropertyService propertyService;

    @PostMapping
    @Operation(summary = "Tạo nhà trọ (người tạo là chủ trọ)")
    ApiResponse<PropertyResponse> create(@RequestBody @Valid PropertyRequest request) {
        return ApiResponse.ok(propertyService.create(request));
    }

    @GetMapping
    @Operation(summary = "Nhà trọ của tôi")
    ApiResponse<List<PropertyResponse>> listMine() {
        return ApiResponse.ok(propertyService.listMine());
    }

    @GetMapping("/{propertyId}")
    ApiResponse<PropertyResponse> get(@PathVariable UUID propertyId) {
        return ApiResponse.ok(propertyService.get(propertyId));
    }

    @PutMapping("/{propertyId}")
    @Operation(summary = "Sửa nhà trọ (chủ trọ)")
    ApiResponse<PropertyResponse> update(@PathVariable UUID propertyId, @RequestBody @Valid PropertyRequest request) {
        return ApiResponse.ok(propertyService.update(propertyId, request));
    }

    @DeleteMapping("/{propertyId}")
    @Operation(summary = "Xóa nhà trọ (chủ trọ)")
    ApiResponse<Void> delete(@PathVariable UUID propertyId) {
        propertyService.delete(propertyId);
        return ApiResponse.ok("Đã xóa nhà trọ");
    }

    @GetMapping("/{propertyId}/members")
    ApiResponse<List<MemberResponse>> listMembers(@PathVariable UUID propertyId) {
        return ApiResponse.ok(propertyService.listMembers(propertyId));
    }

    @PostMapping("/{propertyId}/members")
    @Operation(summary = "Thêm người quản lý theo tên đăng nhập (chủ trọ)")
    ApiResponse<MemberResponse> addMember(@PathVariable UUID propertyId, @RequestBody @Valid MemberRequest request) {
        return ApiResponse.ok(propertyService.addMember(propertyId, request));
    }

    @DeleteMapping("/{propertyId}/members/{userId}")
    @Operation(summary = "Xóa người quản lý (chủ trọ)")
    ApiResponse<Void> removeMember(@PathVariable UUID propertyId, @PathVariable UUID userId) {
        propertyService.removeMember(propertyId, userId);
        return ApiResponse.ok("Đã xóa thành viên");
    }
}