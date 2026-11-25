package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.trohub.entity.Invoice;
import me.ihqqq.trohub.entity.PaymentTransaction;
import me.ihqqq.trohub.enums.*;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.repository.InvoiceRepository;
import me.ihqqq.trohub.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OnlinePaymentSupport {

    InvoiceRepository invoiceRepository;
    PaymentTransactionRepository transactionRepository;
    PaymentService paymentService;

    public record PendingPayment(UUID transactionId, String txnRef, BigDecimal amount, String roomCode) {}

    @Transactional
    public PendingPayment preparePending(UUID propertyId, UUID invoiceId, BigDecimal requestedAmount,
                                         PaymentProvider provider, String txnRef,
                                         BigDecimal minAmount, BigDecimal maxAmount) {
        Invoice invoice = invoiceRepository.findByIdAndPropertyIdAndDeletedFalse(invoiceId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        if (invoice.getStatus() != InvoiceStatus.ISSUED && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID) {
            throw new AppException(ErrorCode.INVOICE_NOT_PAYABLE);
        }

        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        BigDecimal amount = requestedAmount != null ? requestedAmount : remaining;
        if (amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 0) {
            throw new AppException(ErrorCode.PAYMENT_AMOUNT_INVALID);
        }
        if (amount.compareTo(remaining) > 0) {
            throw new AppException(ErrorCode.PAYMENT_EXCEEDS_REMAINING);
        }
        if ((minAmount != null && amount.compareTo(minAmount) < 0)
                || (maxAmount != null && amount.compareTo(maxAmount) > 0)) {
            throw new AppException(ErrorCode.PAYMENT_AMOUNT_OUT_OF_RANGE);
        }

        PaymentTransaction tx = new PaymentTransaction();
        tx.setInvoice(invoice);
        tx.setProvider(provider);
        tx.setTxnRef(txnRef);
        tx.setAmount(amount);
        tx.setStatus(PaymentTransactionStatus.PENDING);
        transactionRepository.save(tx);
        return new PendingPayment(tx.getId(), txnRef, amount, invoice.getRoom().getCode());
    }

    @Transactional
    public void markFailed(UUID transactionId, String reason) {
        transactionRepository.findById(transactionId).ifPresent(tx -> {
            if (tx.getStatus() == PaymentTransactionStatus.PENDING) {
                tx.setStatus(PaymentTransactionStatus.FAILED);
                tx.setRawIpn(reason == null ? null : reason.substring(0, Math.min(reason.length(), 1000)));
                transactionRepository.save(tx);
            }
        });
    }

    @Transactional
    public IpnResult settle(PaymentProvider provider, String txnRef, BigDecimal paidAmount, boolean stillPending,
                            boolean success, String providerTxnNo, String raw, PaymentMethod method) {
        PaymentTransaction tx = transactionRepository.findForUpdate(provider, txnRef).orElse(null);
        if (tx == null) {
            return IpnResult.NOT_FOUND;
        }
        if (tx.getAmount().compareTo(paidAmount) != 0) {
            return IpnResult.AMOUNT_MISMATCH;
        }
        if (tx.getStatus() != PaymentTransactionStatus.PENDING) {
            return IpnResult.ALREADY_PROCESSED;
        }
        if (stillPending) {
            return IpnResult.IGNORED_PENDING;
        }

        tx.setProviderTxnNo(providerTxnNo);
        tx.setRawIpn(raw);
        if (success) {
            Invoice invoice = tx.getInvoice();
            var applied = paymentService.tryApplyPayment(invoice.getProperty().getId(), invoice.getId(),
                    tx.getAmount(), method, provider + " GD " + providerTxnNo);
            tx.setStatus(PaymentTransactionStatus.SUCCESS);
            if (applied.isEmpty()) {
                log.warn("{} txn {} thanh toán thành công nhưng không ghi nhận được vào hóa đơn {}: cần đối soát/hoàn tiền",
                        provider, txnRef, invoice.getId());
            }
        } else {
            tx.setStatus(PaymentTransactionStatus.FAILED);
        }
        transactionRepository.save(tx);
        return IpnResult.SETTLED;
    }
}