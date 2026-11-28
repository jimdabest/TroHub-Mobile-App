package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.ContractCreationRequest;
import me.ihqqq.trohub.dto.request.ContractOccupantsRequest;
import me.ihqqq.trohub.dto.request.ContractRenewRequest;
import me.ihqqq.trohub.dto.request.DepositSettlementRequest;
import me.ihqqq.trohub.dto.request.TerminateContractRequest;
import me.ihqqq.trohub.dto.response.ContractTerminationResponse;
import me.ihqqq.trohub.dto.response.PageResponse;
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
    @Operation(summary = "Danh sách hợp đồng; expiringWithinDays=N lọc hợp đồng ACTIVE sắp hoặc đã quá ngày kết thúc dự kiến")
    ApiResponse<PageResponse<ContractResponse>> list(@PathVariable UUID propertyId,
                                                     @RequestParam(required = false) ContractStatus status,
                                                     @RequestParam(required = false) Integer expiringWithinDays,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(contractService.list(propertyId, status, expiringWithinDays, page, size));
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

    @PatchMapping("/{contractId}/occupants")
    @Operation(summary = "Cập nhật số người ở (dùng tính dịch vụ theo đầu người từ kỳ hóa đơn sau)")
    ApiResponse<ContractResponse> updateOccupants(@PathVariable UUID propertyId, @PathVariable UUID contractId,
                                                  @RequestBody @Valid ContractOccupantsRequest request) {
        return ApiResponse.ok(contractService.updateOccupants(propertyId, contractId, request));
    }

    @PostMapping("/{contractId}/renew")
    @Operation(summary = "Gia hạn hợp đồng (đổi ngày kết thúc dự kiến; tiền thuê mới chỉ OWNER)")
    ApiResponse<ContractResponse> renew(@PathVariable UUID propertyId, @PathVariable UUID contractId,
                                        @RequestBody @Valid ContractRenewRequest request) {
        return ApiResponse.ok(contractService.renew(propertyId, contractId, request));
    }

    @PostMapping("/{contractId}/terminate")
    @Operation(summary = "Kết thúc hợp đồng (phòng về AVAILABLE); body tùy chọn: ngày rời đi và quyết toán cọc. Trả kèm công nợ còn lại")
    ApiResponse<ContractTerminationResponse> terminate(@PathVariable UUID propertyId, @PathVariable UUID contractId,
                                                       @RequestBody(required = false) @Valid TerminateContractRequest request) {
        return ApiResponse.ok(contractService.terminate(propertyId, contractId, request));
    }

    @PutMapping("/{contractId}/deposit-settlement")
    @Operation(summary = "Quyết toán / sửa quyết toán tiền cọc của hợp đồng đã kết thúc (chỉ OWNER)")
    ApiResponse<ContractResponse> settleDeposit(@PathVariable UUID propertyId, @PathVariable UUID contractId,
                                                @RequestBody @Valid DepositSettlementRequest request) {
        return ApiResponse.ok(contractService.settleDeposit(propertyId, contractId, request));
    }
}