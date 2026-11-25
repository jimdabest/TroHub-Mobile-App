package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OnlinePaymentResponse {
    String txnRef;
    String paymentUrl;
    BigDecimal amount;
    Instant expiresAt;
    String deeplink;
}