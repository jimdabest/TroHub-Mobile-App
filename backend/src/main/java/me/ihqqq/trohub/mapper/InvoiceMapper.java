package me.ihqqq.trohub.mapper;

import me.ihqqq.trohub.dto.response.InvoiceItemResponse;
import me.ihqqq.trohub.dto.response.InvoiceResponse;
import me.ihqqq.trohub.dto.response.InvoiceSummaryResponse;
import me.ihqqq.trohub.entity.Invoice;
import me.ihqqq.trohub.entity.InvoiceItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

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
}