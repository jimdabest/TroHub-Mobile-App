package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.constant.AppConstant;
import me.ihqqq.trohub.dto.response.RevenueReportResponse;
import me.ihqqq.trohub.dto.response.RevenueReportResponse.MonthRevenue;
import me.ihqqq.trohub.enums.InvoiceStatus;
import me.ihqqq.trohub.enums.RoomStatus;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.repository.InvoiceRepository;
import me.ihqqq.trohub.repository.InvoiceRepository.PeriodTotals;
import me.ihqqq.trohub.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Báo cáo doanh thu. Chỉ OWNER xem được vì là số liệu tài chính của nhà trọ. */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class RevenueReportService {

    static final int DEFAULT_MONTHS = 6;
    static final int MAX_MONTHS = 24;
    /** Hóa đơn tính vào doanh thu: đã phát hành; loại DRAFT và CANCELLED. */
    static final Set<InvoiceStatus> REVENUE_STATUSES =
            EnumSet.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.PAID);
    static final Set<InvoiceStatus> UNPAID_STATUSES =
            EnumSet.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID);

    InvoiceRepository invoiceRepository;
    RoomRepository roomRepository;
    PropertyAccessService accessService;

    /** from/to là tháng bất kỳ trong khoảng (tự chuẩn hóa về ngày 1); mặc định 6 tháng gần nhất đến tháng hiện tại. */
    public RevenueReportResponse revenue(UUID propertyId, LocalDate from, LocalDate to) {
        accessService.requireOwner(propertyId);

        LocalDate today = LocalDate.now(AppConstant.BUSINESS_ZONE);
        LocalDate end = (to != null ? to : today).withDayOfMonth(1);
        LocalDate start = (from != null ? from : end.minusMonths(DEFAULT_MONTHS - 1L)).withDayOfMonth(1);
        long monthCount = ChronoUnit.MONTHS.between(start, end) + 1;
        if (start.isAfter(end) || monthCount > MAX_MONTHS) {
            throw new AppException(ErrorCode.REPORT_RANGE_INVALID);
        }

        Map<LocalDate, PeriodTotals> byPeriod = new HashMap<>();
        for (PeriodTotals row : invoiceRepository.sumTotalsByPeriodRange(propertyId, start, end, REVENUE_STATUSES)) {
            byPeriod.put(row.getPeriod(), row);
        }

        List<MonthRevenue> months = new ArrayList<>();
        BigDecimal totalBilled = BigDecimal.ZERO;
        BigDecimal totalCollected = BigDecimal.ZERO;
        for (LocalDate p = start; !p.isAfter(end); p = p.plusMonths(1)) {
            PeriodTotals row = byPeriod.get(p);
            BigDecimal billed = row == null ? BigDecimal.ZERO : nz(row.getTotal());
            BigDecimal collected = row == null ? BigDecimal.ZERO : nz(row.getPaid());
            months.add(MonthRevenue.builder().period(p).billed(billed).collected(collected)
                    .outstanding(billed.subtract(collected)).build());
            totalBilled = totalBilled.add(billed);
            totalCollected = totalCollected.add(collected);
        }

        var overdue = invoiceRepository.sumOverdue(propertyId, today, UNPAID_STATUSES);
        long totalRooms = roomRepository.countByPropertyIdAndDeletedFalse(propertyId);
        long occupied = roomRepository.countByPropertyIdAndStatusAndDeletedFalse(propertyId, RoomStatus.OCCUPIED);

        return RevenueReportResponse.builder()
                .from(start)
                .to(end)
                .totalBilled(totalBilled)
                .totalCollected(totalCollected)
                .totalOutstanding(totalBilled.subtract(totalCollected))
                .averageCollectedPerMonth(totalCollected.divide(BigDecimal.valueOf(monthCount), 0, RoundingMode.HALF_UP))
                .overdueInvoiceCount(overdue == null || overdue.getInvoiceCount() == null ? 0 : overdue.getInvoiceCount())
                .overdueAmount(overdue == null ? BigDecimal.ZERO : nz(overdue.getAmount()))
                .occupiedRooms(occupied)
                .totalRooms(totalRooms)
                .occupancyRate(totalRooms == 0 ? BigDecimal.ZERO
                        : BigDecimal.valueOf(occupied * 100).divide(BigDecimal.valueOf(totalRooms), 1, RoundingMode.HALF_UP))
                .months(months)
                .build();
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}