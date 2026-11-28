package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.PaymentRequest;
import me.ihqqq.trohub.dto.response.InvoiceSummaryResponse;
import me.ihqqq.trohub.dto.response.PaymentCreationResponse;
import me.ihqqq.trohub.dto.response.PaymentResponse;
import me.ihqqq.trohub.entity.Invoice;
import me.ihqqq.trohub.entity.Payment;
import me.ihqqq.trohub.enums.InvoiceStatus;
import me.ihqqq.trohub.enums.PaymentMethod;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.mapper.InvoiceMapper;
import me.ihqqq.trohub.mapper.PaymentMapper;
import me.ihqqq.trohub.repository.InvoiceRepository;
import me.ihqqq.trohub.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PaymentService {

    PaymentRepository paymentRepository;
    InvoiceRepository invoiceRepository;
    PaymentMapper paymentMapper;
    InvoiceMapper invoiceMapper;
    PropertyAccessService accessService;

    public PaymentCreationResponse create(UUID propertyId, UUID invoiceId, PaymentRequest request) {
        accessService.requireMember(propertyId);
        if (!request.getMethod().isManual()) {
            throw new AppException(ErrorCode.PAYMENT_METHOD_INVALID);
        }
        return applyPayment(propertyId, invoiceId, request.getAmount(), request.getMethod(), request.getNote());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> list(UUID propertyId, UUID invoiceId) {
        accessService.requireMember(propertyId);
        if (!invoiceRepository.existsByIdAndPropertyIdAndDeletedFalse(invoiceId, propertyId)) {
            throw new AppException(ErrorCode.INVOICE_NOT_FOUND);
        }
        return paymentRepository.findAllByInvoiceIdAndDeletedFalseOrderByPaidAtDesc(invoiceId).stream()
                .map(paymentMapper::toResponse).toList();
    }

    public PaymentCreationResponse applyPayment(UUID propertyId, UUID invoiceId, BigDecimal amount,
                                                PaymentMethod method, String note) {
        Invoice invoice = invoiceRepository.findForUpdate(invoiceId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        ErrorCode problem = checkPayable(invoice, amount);
        if (problem != null) {
            throw new AppException(problem);
        }
        return record(invoice, amount, method, note);
    }


    public InvoiceSummaryResponse voidPayment(UUID propertyId, UUID invoiceId, UUID paymentId) {
        accessService.requireOwner(propertyId);
        Invoice invoice = invoiceRepository.findForUpdate(invoiceId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        Payment payment = paymentRepository.findByIdAndInvoiceIdAndDeletedFalse(paymentId, invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getMethod().isManual()) {
            throw new AppException(ErrorCode.PAYMENT_NOT_VOIDABLE);
        }
        payment.setDeleted(true);
        paymentRepository.save(payment);

        invoice.setPaidAmount(invoice.getPaidAmount().subtract(payment.getAmount()).max(BigDecimal.ZERO));
        invoice.refreshStatus();
        return invoiceMapper.toSummary(invoiceRepository.save(invoice));
    }

    public Optional<PaymentCreationResponse> tryApplyPayment(UUID propertyId, UUID invoiceId, BigDecimal amount,
                                                             PaymentMethod method, String note) {
        return invoiceRepository.findForUpdate(invoiceId, propertyId)
                .filter(invoice -> checkPayable(invoice, amount) == null)
                .map(invoice -> record(invoice, amount, method, note));
    }

    private static ErrorCode checkPayable(Invoice invoice, BigDecimal amount) {
        if (invoice.getStatus() != InvoiceStatus.ISSUED && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID) {
            return ErrorCode.INVOICE_NOT_PAYABLE;
        }
        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        return amount.compareTo(remaining) > 0 ? ErrorCode.PAYMENT_EXCEEDS_REMAINING : null;
    }

    private PaymentCreationResponse record(Invoice invoice, BigDecimal amount, PaymentMethod method, String note) {
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setPaidAt(Instant.now());
        payment.setNote(note);
        paymentRepository.save(payment);

        invoice.setPaidAmount(invoice.getPaidAmount().add(amount));
        invoice.refreshStatus();
        invoiceRepository.save(invoice);

        return PaymentCreationResponse.builder()
                .payment(paymentMapper.toResponse(payment))
                .invoice(invoiceMapper.toSummary(invoice))
                .build();
    }
}