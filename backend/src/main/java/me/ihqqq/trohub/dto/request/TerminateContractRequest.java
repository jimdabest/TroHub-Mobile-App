package me.ihqqq.trohub.dto.request;

import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

/** Tất cả đều tùy chọn: bỏ trống endDate = hôm nay; bỏ trống depositSettlement = chưa quyết toán cọc, làm sau. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TerminateContractRequest {
    /** Ngày khách thực tế rời đi, từ ngày bắt đầu hợp đồng đến hôm nay. */
    LocalDate endDate;
    @Valid
    DepositSettlementRequest depositSettlement;
}