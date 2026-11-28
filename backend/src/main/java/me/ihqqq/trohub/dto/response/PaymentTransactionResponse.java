package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.entity.PaymentTransaction;
import me.ihqqq.trohub.enums.PaymentProvider;
import me.ihqqq.trohub.enums.PaymentTransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransactionResponse {
    UUID id;
    UUID invoiceId;
    String roomCode;
    PaymentProvider provider;
    String txnRef;
    BigDecimal amount;
    PaymentTransactionStatus status;
    String providerTxnNo;
    Instant createdAt;
    Instant updatedAt;

    public static PaymentTransactionResponse from(PaymentTransaction tx) {
        return PaymentTransactionResponse.builder()
                .id(tx.getId())
                .invoiceId(tx.getInvoice().getId())
                .roomCode(tx.getInvoice().getRoom().getCode())
                .provider(tx.getProvider())
                .txnRef(tx.getTxnRef())
                .amount(tx.getAmount())
                .status(tx.getStatus())
                .providerTxnNo(tx.getProviderTxnNo())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .build();
    }
}