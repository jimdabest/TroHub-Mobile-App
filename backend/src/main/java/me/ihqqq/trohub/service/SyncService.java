package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.response.*;
import me.ihqqq.trohub.entity.BaseEntity;
import me.ihqqq.trohub.entity.Invoice;
import me.ihqqq.trohub.mapper.*;
import me.ihqqq.trohub.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Kéo thay đổi của một nhà trọ kể từ một thời điểm, dựa trên updated_at và cờ deleted (xóa mềm) có sẵn ở mọi bảng.
 * Chỉ một chiều máy chủ -> thiết bị; mọi thay đổi từ thiết bị vẫn gửi qua các API hiện có.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class SyncService {

    static final Duration OVERLAP = Duration.ofSeconds(5);

    RoomRepository roomRepository;
    TenantRepository tenantRepository;
    ServicePriceRepository servicePriceRepository;
    ContractRepository contractRepository;
    MeterReadingRepository meterReadingRepository;
    InvoiceRepository invoiceRepository;
    PaymentRepository paymentRepository;
    RoomMapper roomMapper;
    TenantMapper tenantMapper;
    ServicePriceMapper servicePriceMapper;
    ContractMapper contractMapper;
    MeterReadingMapper meterReadingMapper;
    InvoiceMapper invoiceMapper;
    PaymentMapper paymentMapper;
    PropertyAccessService accessService;

    public SyncResponse pull(UUID propertyId, Instant since) {
        accessService.requireMember(propertyId);
        boolean full = since == null;
        Instant from = full ? Instant.EPOCH : since;
        Instant syncedAt = Instant.now().minus(OVERLAP);   // lấy trước khi truy vấn

        return SyncResponse.builder()
                .syncedAt(syncedAt)
                .full(full)
                .rooms(changes(roomRepository.findAllByPropertyIdAndUpdatedAtAfter(propertyId, from), full,
                        roomMapper::toResponse))
                .tenants(changes(tenantRepository.findAllByPropertyIdAndUpdatedAtAfter(propertyId, from), full,
                        tenantMapper::toResponse))
                .servicePrices(changes(servicePriceRepository.findAllByPropertyIdAndUpdatedAtAfter(propertyId, from),
                        full, servicePriceMapper::toResponse))
                .contracts(changes(contractRepository.findAllChangedSince(propertyId, from), full,
                        contractMapper::toResponse))
                .meterReadings(changes(meterReadingRepository.findAllChangedSince(propertyId, from), full,
                        meterReadingMapper::toResponse))
                .invoices(changes(invoiceRepository.findAllChangedSince(propertyId, from), full, this::invoiceDetail))
                .payments(changes(paymentRepository.findAllChangedSince(propertyId, from), full,
                        paymentMapper::toResponse))
                .build();
    }

    /** Kéo toàn bộ (full) thì bỏ các dòng đã xóa: client chưa có gì để xóa. */
    private <E extends BaseEntity, R> SyncChanges<R> changes(List<E> rows, boolean full, Function<E, R> mapper) {
        List<R> upserts = new ArrayList<>();
        List<UUID> deletedIds = new ArrayList<>();
        for (E row : rows) {
            if (row.isDeleted()) {
                if (!full) deletedIds.add(row.getId());
            } else {
                upserts.add(mapper.apply(row));
            }
        }
        return SyncChanges.<R>builder().upserts(upserts).deletedIds(deletedIds).build();
    }

    private InvoiceResponse invoiceDetail(Invoice invoice) {
        InvoiceResponse response = invoiceMapper.toResponse(invoice);
        response.getItems().sort(Comparator.comparing(InvoiceItemResponse::getItemType));
        return response;
    }
}