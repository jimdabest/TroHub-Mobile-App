package me.ihqqq.trohub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.trohub.constant.AppConstant;
import me.ihqqq.trohub.enums.PaymentTransactionStatus;
import me.ihqqq.trohub.repository.PaymentTransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentTransactionCleanupTask {

    private final PaymentTransactionRepository transactionRepository;

    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT1M")
    @Transactional
    public void expirePending() {
        Instant now = Instant.now();
        int expired = transactionRepository.expirePending(PaymentTransactionStatus.PENDING,
                PaymentTransactionStatus.EXPIRED, now.minus(AppConstant.PENDING_PAYMENT_TTL), now);
        if (expired > 0) {
            log.info("Đã đánh dấu hết hạn {} giao dịch thanh toán online", expired);
        }
    }
}