package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SyncResponse {
    /**
     * Giá trị client lưu lại và gửi làm `since` ở lần kéo sau. Đã lùi vài giây so với giờ máy chủ để không bỏ sót
     * dòng vừa commit; dữ liệu lặp lại giữa hai lần kéo là bình thường vì client upsert theo id.
     */
    Instant syncedAt;
    /** true khi không truyền since (kéo toàn bộ): không có deletedIds. */
    boolean full;
    SyncChanges<RoomResponse> rooms;
    SyncChanges<TenantResponse> tenants;
    SyncChanges<ServicePriceResponse> servicePrices;
    SyncChanges<ContractResponse> contracts;
    SyncChanges<MeterReadingResponse> meterReadings;
    SyncChanges<InvoiceResponse> invoices;
    SyncChanges<PaymentResponse> payments;
}