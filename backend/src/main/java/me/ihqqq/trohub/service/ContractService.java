package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.constant.AppConstant;
import me.ihqqq.trohub.dto.request.ContractCreationRequest;
import me.ihqqq.trohub.dto.request.ContractOccupantsRequest;
import me.ihqqq.trohub.dto.request.ContractRenewRequest;
import me.ihqqq.trohub.dto.request.DepositSettlementRequest;
import me.ihqqq.trohub.dto.request.TerminateContractRequest;
import me.ihqqq.trohub.dto.response.ContractTerminationResponse;
import me.ihqqq.trohub.dto.response.PageResponse;
import me.ihqqq.trohub.util.PagingUtils;
import me.ihqqq.trohub.dto.response.ContractResponse;
import me.ihqqq.trohub.entity.Contract;
import me.ihqqq.trohub.entity.Room;
import me.ihqqq.trohub.entity.Tenant;
import me.ihqqq.trohub.enums.ContractStatus;
import me.ihqqq.trohub.enums.InvoiceStatus;
import me.ihqqq.trohub.enums.RoomStatus;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.mapper.ContractMapper;
import me.ihqqq.trohub.repository.ContractRepository;
import me.ihqqq.trohub.repository.InvoiceRepository;
import me.ihqqq.trohub.repository.RoomRepository;
import me.ihqqq.trohub.repository.TenantRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.EnumSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ContractService {

    ContractRepository contractRepository;
    RoomRepository roomRepository;
    TenantRepository tenantRepository;
    ContractMapper contractMapper;
    InvoiceRepository invoiceRepository;
    PropertyAccessService accessService;

    @Transactional(readOnly = true)
    public PageResponse<ContractResponse> list(UUID propertyId, ContractStatus status, Integer expiringWithinDays,
                                               int page, int size) {
        accessService.requireMember(propertyId);
        PagingUtils.validate(page, size);
        if (expiringWithinDays != null && expiringWithinDays < 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        List<Contract> contracts = status == null
                ? contractRepository.findAllByPropertyIdAndDeletedFalseOrderByStartDateDesc(propertyId)
                : contractRepository.findAllByPropertyIdAndStatusAndDeletedFalseOrderByStartDateDesc(propertyId, status);
        if (expiringWithinDays != null) {
            // Hợp đồng ACTIVE có ngày kết thúc dự kiến trong N ngày tới, kể cả đã quá hạn
            LocalDate limit = LocalDate.now(AppConstant.BUSINESS_ZONE).plusDays(expiringWithinDays);
            contracts = contracts.stream()
                    .filter(c -> c.getStatus() == ContractStatus.ACTIVE && c.getEndDate() != null
                            && !c.getEndDate().isAfter(limit))
                    .toList();
        }
        // Bộ lọc expiringWithinDays tính theo ngày nên làm trong bộ nhớ; chỉ ánh xạ các hợp đồng của trang trả về
        return PageResponse.of(contracts, page, size, contractMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ContractResponse get(UUID propertyId, UUID contractId) {
        accessService.requireMember(propertyId);
        return contractMapper.toResponse(find(propertyId, contractId));
    }

    public ContractResponse create(UUID propertyId, ContractCreationRequest request) {
        var access = accessService.requireMember(propertyId);

        Room room = roomRepository.findByIdAndPropertyIdAndDeletedFalse(request.getRoomId(), propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        Tenant tenant = tenantRepository.findByIdAndPropertyIdAndDeletedFalse(request.getTenantId(), propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new AppException(ErrorCode.ROOM_NOT_AVAILABLE);
        }
        if (request.getEndDate() != null && !request.getEndDate().isAfter(request.getStartDate())) {
            throw new AppException(ErrorCode.CONTRACT_DATE_INVALID);
        }

        Contract contract = new Contract();
        contract.setProperty(access.property());
        contract.setRoom(room);
        contract.setTenant(tenant);
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setMonthlyRent(request.getMonthlyRent() != null ? request.getMonthlyRent() : room.getPrice());
        contract.setDeposit(request.getDeposit() != null ? request.getDeposit() : BigDecimal.ZERO);
        contract.setOccupants(request.getOccupants() != null ? request.getOccupants() : 1);
        contract.setNote(request.getNote());
        contract.setStatus(ContractStatus.ACTIVE);

        room.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room);

        try {
            return contractMapper.toResponse(contractRepository.saveAndFlush(contract));
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.ROOM_NOT_AVAILABLE);
        }
    }

    /** Cập nhật số người ở; chỉ ảnh hưởng hóa đơn được tạo từ lúc này, không sửa hóa đơn đã phát hành. */
    public ContractResponse updateOccupants(UUID propertyId, UUID contractId, ContractOccupantsRequest request) {
        accessService.requireMember(propertyId);
        Contract contract = find(propertyId, contractId);
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new AppException(ErrorCode.CONTRACT_NOT_ACTIVE);
        }
        contract.setOccupants(request.getOccupants());
        return contractMapper.toResponse(contractRepository.save(contract));
    }

    /**
     * Gia hạn hợp đồng đang hiệu lực. Hợp đồng quá ngày kết thúc dự kiến KHÔNG tự động kết thúc (khách thường
     * vẫn ở): chủ trọ chọn gia hạn ở đây hoặc kết thúc hợp đồng. Đổi tiền thuê cần quyền OWNER.
     */
    public ContractResponse renew(UUID propertyId, UUID contractId, ContractRenewRequest request) {
        accessService.requireMember(propertyId);
        if (request.getMonthlyRent() != null) {
            accessService.requireOwner(propertyId);
        }
        Contract contract = find(propertyId, contractId);
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new AppException(ErrorCode.CONTRACT_NOT_ACTIVE);
        }
        LocalDate today = LocalDate.now(AppConstant.BUSINESS_ZONE);
        if (!request.getNewEndDate().isAfter(today) || !request.getNewEndDate().isAfter(contract.getStartDate())) {
            throw new AppException(ErrorCode.CONTRACT_RENEW_DATE_INVALID);
        }
        contract.setEndDate(request.getNewEndDate());
        if (request.getMonthlyRent() != null) {
            contract.setMonthlyRent(request.getMonthlyRent());
        }
        return contractMapper.toResponse(contractRepository.save(contract));
    }

    /**
     * Kết thúc hợp đồng. endDate là ngày khách thực tế rời đi (mặc định hôm nay), không còn giữ ngày kết thúc dự kiến
     * cũ, để tháng cuối được tính theo số ngày ở thực tế. Có thể quyết toán cọc ngay hoặc làm sau.
     * Kết quả kèm hóa đơn còn nợ của hợp đồng và việc có cần tạo hóa đơn tháng cuối không.
     */
    public ContractTerminationResponse terminate(UUID propertyId, UUID contractId, TerminateContractRequest request) {
        accessService.requireOwner(propertyId);
        Contract contract = find(propertyId, contractId);
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new AppException(ErrorCode.CONTRACT_NOT_ACTIVE);
        }

        LocalDate today = LocalDate.now(AppConstant.BUSINESS_ZONE);
        LocalDate endDate = request != null && request.getEndDate() != null ? request.getEndDate() : today;
        if (endDate.isBefore(contract.getStartDate()) || endDate.isAfter(today)) {
            if (request != null && request.getEndDate() != null) {
                throw new AppException(ErrorCode.CONTRACT_END_DATE_INVALID);
            }
            endDate = contract.getStartDate();   // hôm nay trước ngày bắt đầu (hợp đồng đặt trước): kết thúc ngay ngày bắt đầu
        }
        contract.setEndDate(endDate);
        contract.setStatus(ContractStatus.ENDED);
        if (request != null && request.getDepositSettlement() != null) {
            applyDepositSettlement(contract, request.getDepositSettlement());
        }

        Room room = contract.getRoom();
        room.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);
        Contract saved = contractRepository.save(contract);

        var outstanding = invoiceRepository.sumOutstandingByContract(contractId,
                EnumSet.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID));
        boolean finalInvoicePending = !invoiceRepository
                .findInvoicedRoomIds(propertyId, endDate.withDayOfMonth(1)).contains(room.getId());
        return ContractTerminationResponse.builder()
                .contract(contractMapper.toResponse(saved))
                .outstandingInvoiceCount(outstanding == null || outstanding.getInvoiceCount() == null
                        ? 0 : outstanding.getInvoiceCount())
                .outstandingAmount(outstanding == null || outstanding.getAmount() == null
                        ? BigDecimal.ZERO : outstanding.getAmount())
                .finalInvoicePending(finalInvoicePending)
                .build();
    }

    /** Quyết toán (hoặc sửa quyết toán) tiền cọc cho hợp đồng đã kết thúc. Chỉ OWNER. */
    public ContractResponse settleDeposit(UUID propertyId, UUID contractId, DepositSettlementRequest request) {
        accessService.requireOwner(propertyId);
        Contract contract = find(propertyId, contractId);
        if (contract.getStatus() != ContractStatus.ENDED) {
            throw new AppException(ErrorCode.CONTRACT_NOT_ENDED);
        }
        applyDepositSettlement(contract, request);
        return contractMapper.toResponse(contractRepository.save(contract));
    }

    private void applyDepositSettlement(Contract contract, DepositSettlementRequest request) {
        BigDecimal refunded = request.getDepositRefunded();
        BigDecimal deducted = request.getDepositDeducted();
        if (refunded == null || deducted == null || refunded.signum() < 0 || deducted.signum() < 0
                || refunded.add(deducted).compareTo(contract.getDeposit()) > 0) {
            throw new AppException(ErrorCode.DEPOSIT_SETTLEMENT_INVALID);
        }
        contract.setDepositRefunded(refunded);
        contract.setDepositDeducted(deducted);
        contract.setDepositNote(request.getDepositNote());
    }

    private Contract find(UUID propertyId, UUID contractId) {
        return contractRepository.findByIdAndPropertyIdAndDeletedFalse(contractId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));
    }
}