package me.ihqqq.trohub.mapper;

import me.ihqqq.trohub.dto.response.InvoiceItemResponse;
import me.ihqqq.trohub.dto.response.InvoiceResponse;
import me.ihqqq.trohub.dto.response.InvoiceSummaryResponse;
import me.ihqqq.trohub.entity.Invoice;
import me.ihqqq.trohub.entity.InvoiceItem;
import me.ihqqq.trohub.constant.AppConstant;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InvoiceMapper {

    @Mapping(source = "property.id", target = "propertyId")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.code", target = "roomCode")
    @Mapping(source = "contract.id", target = "contractId")
    @Mapping(target = "remainingAmount", expression = "java(invoice.getTotalAmount().subtract(invoice.getPaidAmount()))")
    InvoiceSummaryResponse toSummary(Invoice invoice);

    @Mapping(source = "property.id", target = "propertyId")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.code", target = "roomCode")
    @Mapping(source = "contract.id", target = "contractId")
    @Mapping(target = "remainingAmount", expression = "java(invoice.getTotalAmount().subtract(invoice.getPaidAmount()))")
    InvoiceResponse toResponse(Invoice invoice);

    InvoiceItemResponse toItemResponse(InvoiceItem item);

    @AfterMapping
    default void fillOverdue(Invoice invoice, @MappingTarget InvoiceSummaryResponse target) {
        LocalDate today = LocalDate.now(AppConstant.BUSINESS_ZONE);
        boolean overdue = invoice.isOverdue(today);
        target.setOverdue(overdue);
        target.setDaysOverdue(overdue ? ChronoUnit.DAYS.between(invoice.getDueDate(), today) : 0);
    }

    @AfterMapping
    default void fillOverdue(Invoice invoice, @MappingTarget InvoiceResponse target) {
        LocalDate today = LocalDate.now(AppConstant.BUSINESS_ZONE);
        boolean overdue = invoice.isOverdue(today);
        target.setOverdue(overdue);
        target.setDaysOverdue(overdue ? ChronoUnit.DAYS.between(invoice.getDueDate(), today) : 0);
    }
}