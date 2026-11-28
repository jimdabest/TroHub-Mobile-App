package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OnlinePaymentResponse {
    UUID transactionId;   // dùng để app gọi API tra trạng thái giao dịch
    String txnRef;
    String paymentUrl;
    BigDecimal amount;
    Instant expiresAt;
    String deeplink;
}