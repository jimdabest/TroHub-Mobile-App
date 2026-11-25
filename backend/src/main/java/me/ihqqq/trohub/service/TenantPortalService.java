package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.OnlinePaymentRequest;
import me.ihqqq.trohub.dto.response.*;
import me.ihqqq.trohub.entity.Contract;
import me.ihqqq.trohub.entity.Property;
import me.ihqqq.trohub.entity.User;
import me.ihqqq.trohub.enums.InvoiceStatusFilter;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.mapper.InvoiceMapper;
import me.ihqqq.trohub.mapper.PaymentMapper;
import me.ihqqq.trohub.repository.*;
import me.ihqqq.trohub.util.PhoneUtils;
import me.ihqqq.trohub.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Phân hệ khách thuê. Quyền truy cập KHÔNG dựa trên property_member mà dựa trên số điện thoại:
 * tài khoản đang đăng nhập thấy dữ liệu của các hồ sơ người thuê có cùng số điện thoại (đã chuẩn hóa).
 * Mọi truy vấn đều ràng buộc theo tập tenantId đó nên khách không bao giờ thấy hóa đơn của người khác;
 * tìm không thấy được trả 404 như hóa đơn không tồn tại.
 *
 * Không đặt @Transactional ở cấp class: các hàm gọi cổng thanh toán phải nằm ngoài transaction chỉ-đọc.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TenantPortalService {

    UserRepository userRepository;
    TenantRepository tenantRepository;
    ContractRepository contractRepository;
    InvoiceRepository invoiceRepository;
    PaymentRepository paymentRepository;
    InvoiceMapper invoiceMapper;
    PaymentMapper paymentMapper;
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
        return invoices.stream().map(invoiceMapper::toSummary).toList();
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
        User user = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String phone = PhoneUtils.normalize(user.getPhone());
        if (phone == null) {
            throw new AppException(ErrorCode.TENANT_PHONE_REQUIRED);
        }
        return tenantRepository.findIdsByPhoneNormalized(phone);
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