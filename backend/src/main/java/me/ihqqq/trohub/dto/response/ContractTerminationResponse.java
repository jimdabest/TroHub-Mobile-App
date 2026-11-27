package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractTerminationResponse {
    ContractResponse contract;
    /** Hóa đơn của hợp đồng này còn chưa thanh toán đủ (vẫn thu được sau khi kết thúc hợp đồng). */
    long outstandingInvoiceCount;
    BigDecimal outstandingAmount;
    /** Tháng rời đi chưa có hóa đơn: ghi chỉ số điện nước cuối rồi tạo hóa đơn kỳ đó để thu tháng cuối theo số ngày ở. */
    boolean finalInvoicePending;
}