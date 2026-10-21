package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.UserCreationRequest;
import me.ihqqq.trohub.dto.request.UserUpdateRequest;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.UserResponse;
import me.ihqqq.trohub.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Users")
public class UserController {

    UserService userService;

    @PostMapping
    @Operation(summary = "Đăng ký tài khoản")
    @SecurityRequirements
    ApiResponse<UserResponse> register(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.ok(userService.createUser(request));
    }

    @GetMapping("/myInfo")
    @Operation(summary = "Thông tin tài khoản đang đăng nhập")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.ok(userService.getMyInfo());
    }

    @PutMapping("/myInfo")
    @Operation(summary = "Cập nhật thông tin cá nhân")
    ApiResponse<UserResponse> updateMyInfo(@RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.ok(userService.updateMyInfo(request));
    }

    @GetMapping
    @Operation(summary = "Danh sách người dùng (chỉ ADMIN hệ thống)")
    ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.ok(userService.getUsers());
    }
}