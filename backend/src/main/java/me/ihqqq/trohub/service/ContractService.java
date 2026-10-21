package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.constant.AppConstant;
import me.ihqqq.trohub.dto.request.ContractCreationRequest;
import me.ihqqq.trohub.dto.response.ContractResponse;
import me.ihqqq.trohub.entity.Contract;
import me.ihqqq.trohub.entity.Room;
import me.ihqqq.trohub.entity.Tenant;
import me.ihqqq.trohub.enums.ContractStatus;
import me.ihqqq.trohub.enums.RoomStatus;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.mapper.ContractMapper;
import me.ihqqq.trohub.repository.ContractRepository;
import me.ihqqq.trohub.repository.RoomRepository;
import me.ihqqq.trohub.repository.TenantRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    PropertyAccessService accessService;

    @Transactional(readOnly = true)
    public List<ContractResponse> list(UUID propertyId, ContractStatus status) {
        accessService.requireMember(propertyId);
        List<Contract> contracts = status == null
                ? contractRepository.findAllByPropertyIdAndDeletedFalseOrderByStartDateDesc(propertyId)
                : contractRepository.findAllByPropertyIdAndStatusAndDeletedFalseOrderByStartDateDesc(propertyId, status);
        return contracts.stream().map(contractMapper::toResponse).toList();
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

    public ContractResponse terminate(UUID propertyId, UUID contractId) {
        accessService.requireMember(propertyId);
        Contract contract = find(propertyId, contractId);
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new AppException(ErrorCode.CONTRACT_NOT_ACTIVE);
        }

        LocalDate today = LocalDate.now(AppConstant.BUSINESS_ZONE);
        if (contract.getEndDate() == null || contract.getEndDate().isAfter(today)) {
            contract.setEndDate(today.isBefore(contract.getStartDate()) ? contract.getStartDate() : today);
        }
        contract.setStatus(ContractStatus.ENDED);

        Room room = contract.getRoom();
        room.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);

        return contractMapper.toResponse(contractRepository.save(contract));
    }

    private Contract find(UUID propertyId, UUID contractId) {
        return contractRepository.findByIdAndPropertyIdAndDeletedFalse(contractId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));
    }
}