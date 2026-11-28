package me.ihqqq.trohub.mapper;

import me.ihqqq.trohub.dto.response.PaymentResponse;
import me.ihqqq.trohub.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(source = "invoice.id", target = "invoiceId")
    PaymentResponse toResponse(Payment payment);
}