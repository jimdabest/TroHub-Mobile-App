package me.ihqqq.trohub.mapper;

import me.ihqqq.trohub.dto.response.ContractResponse;
import me.ihqqq.trohub.constant.AppConstant;
import me.ihqqq.trohub.entity.Contract;
import me.ihqqq.trohub.enums.ContractStatus;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContractMapper {
    @Mapping(source = "property.id", target = "propertyId")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.code", target = "roomCode")
    @Mapping(source = "tenant.id", target = "tenantId")
    @Mapping(source = "tenant.fullName", target = "tenantName")
    ContractResponse toResponse(Contract contract);

    @AfterMapping
    default void fillDepositSettled(Contract contract, @MappingTarget ContractResponse target) {
        target.setDepositSettled(contract.getDepositRefunded() != null && contract.getDepositDeducted() != null);
    }

    @AfterMapping
    default void fillExpiry(Contract contract, @MappingTarget ContractResponse target) {
        if (contract.getStatus() == ContractStatus.ACTIVE && contract.getEndDate() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(AppConstant.BUSINESS_ZONE), contract.getEndDate());
            target.setDaysUntilEnd(days);
            target.setExpired(days < 0);
        }
    }
}