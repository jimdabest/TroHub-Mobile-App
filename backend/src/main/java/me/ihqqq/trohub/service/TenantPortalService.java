package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.OnlinePaymentRequest;
import me.ihqqq.trohub.dto.response.*;
import me.ihqqq.trohub.constant.AppConstant;
import me.ihqqq.trohub.entity.Contract;
import me.ihqqq.trohub.entity.Property;
import me.ihqqq.trohub.enums.InvoiceStatusFilter;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.mapper.InvoiceMapper;
import me.ihqqq.trohub.mapper.PaymentMapper;
import me.ihqqq.trohub.repository.*;
import me.ihqqq.trohub.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TenantPortalService {

    TenantRepository tenantRepository;
    ContractRepository contractRepository;
    InvoiceRepository invoiceRepository;
    PaymentRepository paymentRepository;
    InvoiceMapper invoiceMapper;
    PaymentMapper paymentMapper;
    PaymentTransactionRepository transactionRepository;
    VnPayService vnPayService;
    MomoService momoService;

    @Transactional(readOnly = true)
    public List<TenantContractResponse> contracts() {
        List<UUID> tenantIds = myTenantIds();
        if (tenantIds.isEmpty()) return List.of();
        return contractRepository.findAllForTenants(tenantIds).stream().map(this::toContractResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceSummaryResponse> invoices(LocalDate period, InvoiceStatusFilter filter) {
        List<UUID> tenantIds = myTenantIds();
        if (tenantIds.isEmpty()) return List.of();
        var statuses = (filter == null ? InvoiceStatusFilter.ALL : filter).tenantVisibleStatuses();
        var invoices = period == null
                ? invoiceRepository.findAllForTenants(tenantIds, statuses)
                : invoiceRepository.findAllForTenantsAndPeriod(tenantIds, period.withDayOfMonth(1), statuses);
        LocalDate today = LocalDate.now(AppConstant.BUSINESS_ZONE);
        return invoices.stream()
                .filter(i -> filter != InvoiceStatusFilter.OVERDUE || i.isOverdue(today))
                .map(invoiceMapper::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse invoice(UUID invoiceId) {
        List<UUID> tenantIds = myTenantIds();
        if (tenantIds.isEmpty()) throw new AppException(ErrorCode.INVOICE_NOT_FOUND);
        var invoice = invoiceRepository.findDetailForTenants(invoiceId, tenantIds,
                        InvoiceStatusFilter.ALL.tenantVisibleStatuses())
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        InvoiceResponse response = invoiceMapper.toResponse(invoice);
        response.getItems().sort(Comparator.comparing(InvoiceItemResponse::getItemType));
        return response;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> payments(UUID invoiceId) {
        List<UUID> tenantIds = myTenantIds();
        if (tenantIds.isEmpty()) throw new AppException(ErrorCode.INVOICE_NOT_FOUND);
        invoiceRepository.findPropertyIdForTenants(invoiceId, tenantIds, InvoiceStatusFilter.ALL.tenantVisibleStatuses())
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        return paymentRepository.findAllByInvoiceIdAndDeletedFalseOrderByPaidAtDesc(invoiceId).stream()
                .map(paymentMapper::toResponse).toList();
    }


    @Transactional(readOnly = true)
    public PaymentTransactionResponse transaction(UUID transactionId) {
        List<UUID> tenantIds = myTenantIds();
        if (tenantIds.isEmpty()) throw new AppException(ErrorCode.TRANSACTION_NOT_FOUND);
        return PaymentTransactionResponse.from(transactionRepository.findByIdForTenants(transactionId, tenantIds)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND)));
    }

    public OnlinePaymentResponse payWithVnPay(UUID invoiceId, OnlinePaymentRequest request, String clientIp) {
        UUID propertyId = ownedInvoicePropertyId(invoiceId);
        return vnPayService.createPaymentUrlForTenant(propertyId, invoiceId, request, clientIp);
    }

    public OnlinePaymentResponse payWithMomo(UUID invoiceId, OnlinePaymentRequest request) {
        UUID propertyId = ownedInvoicePropertyId(invoiceId);
        return momoService.createPaymentUrlForTenant(propertyId, invoiceId, request);
    }


    private UUID ownedInvoicePropertyId(UUID invoiceId) {
        List<UUID> tenantIds = myTenantIds();
        if (tenantIds.isEmpty()) throw new AppException(ErrorCode.INVOICE_NOT_FOUND);
        return invoiceRepository.findPropertyIdForTenants(invoiceId, tenantIds,
                        InvoiceStatusFilter.ALL.tenantVisibleStatuses())
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
    }

    private List<UUID> myTenantIds() {
        return tenantRepository.findIdsByUserId(SecurityUtils.currentUserId());
    }

    private TenantContractResponse toContractResponse(Contract c) {
        Property p = c.getProperty();
        return TenantContractResponse.builder()
                .contractId(c.getId())
                .status(c.getStatus())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .monthlyRent(c.getMonthlyRent())
                .deposit(c.getDeposit())
                .roomId(c.getRoom().getId())
                .roomCode(c.getRoom().getCode())
                .propertyId(p.getId())
                .propertyName(p.getName())
                .propertyAddress(p.getAddress())
                .bankName(p.getBankName())
                .bankAccountNumber(p.getBankAccountNumber())
                .bankAccountHolder(p.getBankAccountHolder())
                .build();
    }
}