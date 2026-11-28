package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Báo cáo doanh thu theo kỳ hóa đơn (period): mỗi tháng gồm các hóa đơn của kỳ đó,
 * "collected" là số đã thu được trên các hóa đơn ấy (không phải tiền mặt thực nhận trong tháng).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueReportResponse {
    LocalDate from;                       // ngày đầu tháng bắt đầu
    LocalDate to;                         // ngày đầu tháng kết thúc
    BigDecimal totalBilled;               // tổng đã phát hành
    BigDecimal totalCollected;            // tổng đã thu ("TỔNG ĐÃ THU")
    BigDecimal totalOutstanding;          // tổng còn nợ trong khoảng ("TỔNG CÒN NỢ")
    BigDecimal averageCollectedPerMonth;
    long overdueInvoiceCount;             // toàn bộ hóa đơn quá hạn hiện tại của nhà trọ, không chỉ trong khoảng
    BigDecimal overdueAmount;
    long occupiedRooms;                   // ảnh chụp hiện tại, không theo khoảng
    long totalRooms;
    BigDecimal occupancyRate;             // phần trăm, 1 chữ số thập phân
    List<MonthRevenue> months;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MonthRevenue {
        LocalDate period;
        BigDecimal billed;
        BigDecimal collected;
        BigDecimal outstanding;
    }
}