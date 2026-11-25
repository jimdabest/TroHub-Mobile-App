package me.ihqqq.trohub.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OnlinePaymentRequest {
    /** Số tiền muốn trả; bỏ trống = trả hết phần còn nợ. Phải là số nguyên đồng. */
    BigDecimal amount;
}