package me.ihqqq.trohub.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.RoomRequest;
import me.ihqqq.trohub.dto.request.RoomStatusRequest;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.RoomResponse;
import me.ihqqq.trohub.enums.RoomStatus;
import me.ihqqq.trohub.service.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}/rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Rooms")
public class RoomController {

    RoomService roomService;

    @GetMapping
    @Operation(summary = "Danh sách phòng, lọc theo trạng thái (tùy chọn)")
    ApiResponse<List<RoomResponse>> list(@PathVariable UUID propertyId,
                                         @RequestParam(required = false) RoomStatus status) {
        return ApiResponse.ok(roomService.list(propertyId, status));
    }

    @GetMapping("/{roomId}")
    ApiResponse<RoomResponse> get(@PathVariable UUID propertyId, @PathVariable UUID roomId) {
        return ApiResponse.ok(roomService.get(propertyId, roomId));
    }

    @PostMapping
    ApiResponse<RoomResponse> create(@PathVariable UUID propertyId, @RequestBody @Valid RoomRequest request) {
        return ApiResponse.ok(roomService.create(propertyId, request));
    }

    @PutMapping("/{roomId}")
    ApiResponse<RoomResponse> update(@PathVariable UUID propertyId, @PathVariable UUID roomId,
                                     @RequestBody @Valid RoomRequest request) {
        return ApiResponse.ok(roomService.update(propertyId, roomId, request));
    }

    @PatchMapping("/{roomId}/status")
    @Operation(summary = "Đổi trạng thái AVAILABLE <-> MAINTENANCE (OCCUPIED do hợp đồng quản lý)")
    ApiResponse<RoomResponse> changeStatus(@PathVariable UUID propertyId, @PathVariable UUID roomId,
                                           @RequestBody @Valid RoomStatusRequest request) {
        return ApiResponse.ok(roomService.changeStatus(propertyId, roomId, request));
    }

    @DeleteMapping("/{roomId}")
    ApiResponse<Void> delete(@PathVariable UUID propertyId, @PathVariable UUID roomId) {
        roomService.delete(propertyId, roomId);
        return ApiResponse.ok("Đã xóa phòng");
    }
}