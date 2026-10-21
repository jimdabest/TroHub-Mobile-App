package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.ContractCreationRequest;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.ContractResponse;
import me.ihqqq.trohub.enums.ContractStatus;
import me.ihqqq.trohub.service.ContractService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}/contracts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Contracts")
public class ContractController {

    ContractService contractService;

    @GetMapping
    ApiResponse<List<ContractResponse>> list(@PathVariable UUID propertyId,
                                             @RequestParam(required = false) ContractStatus status) {
        return ApiResponse.ok(contractService.list(propertyId, status));
    }

    @GetMapping("/{contractId}")
    ApiResponse<ContractResponse> get(@PathVariable UUID propertyId, @PathVariable UUID contractId) {
        return ApiResponse.ok(contractService.get(propertyId, contractId));
    }

    @PostMapping
    @Operation(summary = "Tạo hợp đồng (phòng phải đang trống, sẽ chuyển sang OCCUPIED)")
    ApiResponse<ContractResponse> create(@PathVariable UUID propertyId,
                                         @RequestBody @Valid ContractCreationRequest request) {
        return ApiResponse.ok(contractService.create(propertyId, request));
    }

    @PostMapping("/{contractId}/terminate")
    @Operation(summary = "Kết thúc hợp đồng (phòng trở về AVAILABLE)")
    ApiResponse<ContractResponse> terminate(@PathVariable UUID propertyId, @PathVariable UUID contractId) {
        return ApiResponse.ok(contractService.terminate(propertyId, contractId));
    }
}