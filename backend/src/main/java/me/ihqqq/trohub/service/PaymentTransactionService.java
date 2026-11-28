package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.response.PageResponse;
import me.ihqqq.trohub.dto.response.PaymentTransactionResponse;
import me.ihqqq.trohub.entity.PaymentTransaction;
import me.ihqqq.trohub.enums.PaymentTransactionStatus;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.repository.PaymentTransactionRepository;
import me.ihqqq.trohub.util.PagingUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Đối soát giao dịch cổng thanh toán. Chỉ OWNER được xem và xử lý vì liên quan trực tiếp đến tiền.
 * Không trả raw_ipn ra ngoài (chứa dữ liệu thô từ cổng).
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PaymentTransactionService {

    PaymentTransactionRepository transactionRepository;
    PropertyAccessService accessService;

    @Transactional(readOnly = true)
    public PageResponse<PaymentTransactionResponse> list(UUID propertyId, PaymentTransactionStatus status,
                                                         int page, int size) {
        accessService.requireOwner(propertyId);
        var statuses = status == null ? EnumSet.allOf(PaymentTransactionStatus.class) : EnumSet.of(status);
        return PageResponse.from(
                transactionRepository.findPageByProperty(propertyId, statuses, PagingUtils.pageable(page, size)),
                PaymentTransactionResponse::from);
    }

    @Transactional(readOnly = true)
    public PaymentTransactionResponse get(UUID propertyId, UUID transactionId) {
        accessService.requireMember(propertyId);
        return PaymentTransactionResponse.from(transactionRepository.findByIdAndProperty(transactionId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND)));
    }

    /** Đánh dấu đã xử lý một giao dịch thu tiền nhưng không ghi nhận được vào hóa đơn (đã hoàn tiền / xử lý ngoài hệ thống). */
    public PaymentTransactionResponse reconcile(UUID propertyId, UUID transactionId) {
        accessService.requireOwner(propertyId);
        PaymentTransaction tx = transactionRepository.findByIdAndProperty(transactionId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        if (tx.getStatus() != PaymentTransactionStatus.SUCCESS_UNAPPLIED) {
            throw new AppException(ErrorCode.TRANSACTION_NOT_RECONCILABLE);
        }
        tx.setStatus(PaymentTransactionStatus.RECONCILED);
        return PaymentTransactionResponse.from(transactionRepository.save(tx));
    }
}