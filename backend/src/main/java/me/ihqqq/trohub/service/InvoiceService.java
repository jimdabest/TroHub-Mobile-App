package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.constant.AppConstant;
import me.ihqqq.trohub.dto.request.InvoiceGenerationRequest;
import me.ihqqq.trohub.dto.request.InvoiceItemRequest;
import me.ihqqq.trohub.dto.response.InvoiceGenerationResponse;
import me.ihqqq.trohub.dto.response.InvoiceGenerationResponse.SkipReason;
import me.ihqqq.trohub.dto.response.InvoiceGenerationResponse.SkippedRoom;
import me.ihqqq.trohub.dto.response.InvoiceItemResponse;
import me.ihqqq.trohub.dto.response.InvoiceResponse;
import me.ihqqq.trohub.dto.response.InvoiceSummaryResponse;
import me.ihqqq.trohub.entity.*;
import me.ihqqq.trohub.enums.*;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.mapper.InvoiceMapper;
import me.ihqqq.trohub.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class InvoiceService {

    static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MM/yyyy");

    InvoiceRepository invoiceRepository;
    ContractRepository contractRepository;
    RoomRepository roomRepository;
    ServicePriceRepository servicePriceRepository;
    MeterReadingRepository meterReadingRepository;
    InvoiceMapper invoiceMapper;
    PropertyAccessService accessService;
    PaymentTransactionRepository transactionRepository;

    public InvoiceGenerationResponse generate(UUID propertyId, InvoiceGenerationRequest request) {
        var access = accessService.requireMember(propertyId);

        LocalDate period = request.getPeriod().withDayOfMonth(1);
        LocalDate periodEnd = period.plusMonths(1).minusDays(1);
        LocalDate today = LocalDate.now(AppConstant.BUSINESS_ZONE);
        String monthLabel = period.format(MONTH_FORMAT);

        Map<UUID, Room> roomsById = roomRepository.findAllByPropertyIdAndDeletedFalseOrderByCodeAsc(propertyId)
                .stream().collect(Collectors.toMap(Room::getId, r -> r, (a, b) -> a, LinkedHashMap::new));

        Set<UUID> requested = (request.getRoomIds() == null || request.getRoomIds().isEmpty())
                ? null : new LinkedHashSet<>(request.getRoomIds());
        if (requested != null) {
            for (UUID roomId : requested) {
                if (!roomsById.containsKey(roomId)) {
                    throw new AppException(ErrorCode.ROOM_NOT_FOUND);
                }
            }
        }

        // Hợp đồng vừa kết thúc trong kỳ (hóa đơn tháng cuối) rồi mới đến hợp đồng đang hiệu lực: nếu cùng một phòng
        // có cả hai (khách cũ rời, khách mới vào cùng tháng) thì hợp đồng đang hiệu lực được ưu tiên vì mỗi phòng
        // chỉ có một hóa đơn mỗi kỳ.
        Map<UUID, Contract> contractByRoom = new HashMap<>();
        contractRepository.findEndedInPeriodWithRoom(propertyId, ContractStatus.ENDED, period, periodEnd).stream()
                .sorted(Comparator.comparing(Contract::getEndDate))
                .forEach(c -> contractByRoom.put(c.getRoom().getId(), c));
        contractRepository.findAllWithRoomByPropertyAndStatus(propertyId, ContractStatus.ACTIVE).stream()
                .filter(c -> !c.getStartDate().isAfter(periodEnd))
                .forEach(c -> contractByRoom.put(c.getRoom().getId(), c));

        List<ServicePrice> activeServices = servicePriceRepository
                .findAllByPropertyIdAndDeletedFalseOrderByNameAsc(propertyId).stream()
                .filter(ServicePrice::isActive).toList();
        List<ServicePrice> meteredServices = activeServices.stream()
                .filter(s -> MeterReadingService.METERED_TYPES.contains(s.getType())).toList();
        List<ServicePrice> fixedServices = activeServices.stream()
                .filter(s -> s.getType() == ServiceType.FIXED).toList();
        List<ServicePrice> perPersonServices = activeServices.stream()
                .filter(s -> s.getType() == ServiceType.PER_PERSON).toList();

        Map<UUID, Map<UUID, MeterReading>> readingsByRoom = new HashMap<>();
        for (MeterReading m : meterReadingRepository.findAllByPropertyAndPeriod(propertyId, period)) {
            readingsByRoom.computeIfAbsent(m.getRoom().getId(), k -> new HashMap<>())
                    .put(m.getServicePrice().getId(), m);
        }

        Set<UUID> alreadyInvoiced = new HashSet<>(invoiceRepository.findInvoicedRoomIds(propertyId, period));

        List<Invoice> toSave = new ArrayList<>();
        List<SkippedRoom> skipped = new ArrayList<>();

        for (Room room : roomsById.values()) {
            boolean targeted = requested == null ? contractByRoom.containsKey(room.getId())
                    : requested.contains(room.getId());
            if (!targeted) continue;

            if (alreadyInvoiced.contains(room.getId())) {
                skipped.add(skip(room, SkipReason.ALREADY_INVOICED, "Phòng đã có hóa đơn kỳ " + monthLabel));
                continue;
            }
            Contract contract = contractByRoom.get(room.getId());
            if (contract == null) {
                skipped.add(skip(room, SkipReason.NO_ACTIVE_CONTRACT, "Phòng không có hợp đồng đang hiệu lực hoặc kết thúc trong kỳ"));
                continue;
            }

            Map<UUID, MeterReading> roomReadings = readingsByRoom.getOrDefault(room.getId(), Map.of());
            List<String> missing = meteredServices.stream()
                    .filter(s -> !roomReadings.containsKey(s.getId()))
                    .map(ServicePrice::getName).toList();
            if (!missing.isEmpty()) {
                skipped.add(skip(room, SkipReason.MISSING_METER_READING,
                        "Chưa ghi chỉ số: " + String.join(", ", missing)));
                continue;
            }

            Invoice invoice = new Invoice();
            invoice.setProperty(access.property());
            invoice.setRoom(room);
            invoice.setContract(contract);
            invoice.setPeriod(period);
            invoice.setIssueDate(today);
            invoice.setDueDate(request.getDueDate());
            invoice.setStatus(InvoiceStatus.ISSUED);
            invoice.setPaidAmount(BigDecimal.ZERO);

            invoice.addItem(rentItem(contract, period, periodEnd, monthLabel));

            for (ServicePrice service : meteredServices) {
                MeterReading reading = roomReadings.get(service.getId());
                BigDecimal consumption = reading.getCurrentValue().subtract(reading.getPreviousValue());
                String desc = "%s tháng %s (%s → %s %s)".formatted(service.getName(), monthLabel,
                        plain(reading.getPreviousValue()), plain(reading.getCurrentValue()), service.getUnit());
                InvoiceItemType itemType = service.getType() == ServiceType.ELECTRIC
                        ? InvoiceItemType.ELECTRIC : InvoiceItemType.WATER;
                if (service.getTiers().isEmpty()) {
                    invoice.addItem(item(itemType, desc, consumption, service.getUnitPrice()));
                } else if (consumption.signum() <= 0) {
                    invoice.addItem(item(itemType, desc, consumption, service.getTiers().get(0).getUnitPrice()));
                } else {
                    tieredItems(itemType, desc, consumption, service).forEach(invoice::addItem);
                }
            }

            for (ServicePrice service : fixedServices) {
                invoice.addItem(item(InvoiceItemType.SERVICE, service.getName(), BigDecimal.ONE, service.getUnitPrice()));
            }

            int occupants = Math.max(1, contract.getOccupants());
            for (ServicePrice service : perPersonServices) {
                invoice.addItem(item(InvoiceItemType.SERVICE,
                        "%s (%d %s)".formatted(service.getName(), occupants, service.getUnit()),
                        BigDecimal.valueOf(occupants), service.getUnitPrice()));
            }

            invoice.setTotalAmount(invoice.getItems().stream()
                    .map(InvoiceItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            toSave.add(invoice);
        }

        List<Invoice> saved;
        try {
            saved = invoiceRepository.saveAllAndFlush(toSave);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        return InvoiceGenerationResponse.builder()
                .created(saved.stream().map(invoiceMapper::toSummary).toList())
                .skipped(skipped)
                .build();
    }

    @Transactional(readOnly = true)
    public List<InvoiceSummaryResponse> list(UUID propertyId, LocalDate period, InvoiceStatusFilter filter) {
        accessService.requireMember(propertyId);
        var statuses = (filter == null ? InvoiceStatusFilter.ALL : filter).statuses();
        List<Invoice> invoices = period == null
                ? invoiceRepository.findAllByProperty(propertyId, statuses)
                : invoiceRepository.findAllByPropertyAndPeriod(propertyId, period.withDayOfMonth(1), statuses);
        LocalDate today = LocalDate.now(AppConstant.BUSINESS_ZONE);
        return invoices.stream()
                .filter(i -> filter != InvoiceStatusFilter.OVERDUE || i.isOverdue(today))
                .map(invoiceMapper::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse get(UUID propertyId, UUID invoiceId) {
        accessService.requireMember(propertyId);
        InvoiceResponse response = invoiceMapper.toResponse(find(propertyId, invoiceId));
        response.getItems().sort(Comparator.comparing(InvoiceItemResponse::getItemType));
        return response;
    }

    /**
     * Thêm khoản phát sinh hoặc giảm giá (đơn giá âm) vào hóa đơn. Chỉ OWNER vì thay đổi số tiền phải thu.
     * Chỉ hóa đơn chưa thanh toán đủ, không có giao dịch online đang chờ, và tổng mới không thấp hơn số đã thu.
     */
    public InvoiceResponse addAdjustment(UUID propertyId, UUID invoiceId, InvoiceItemRequest request) {
        accessService.requireOwner(propertyId);
        Invoice invoice = lockEditable(propertyId, invoiceId);
        BigDecimal quantity = request.getQuantity() != null
                ? request.getQuantity().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ONE;
        invoice.addItem(item(InvoiceItemType.OTHER, request.getDescription().trim(), quantity, request.getUnitPrice()));
        recalculate(invoice);
        return toDetail(invoiceRepository.save(invoice));
    }

    /** Chỉ xóa được khoản loại OTHER (thêm thủ công); các khoản tự sinh từ hợp đồng và chỉ số không xóa lẻ được. */
    public InvoiceResponse removeAdjustment(UUID propertyId, UUID invoiceId, UUID itemId) {
        accessService.requireOwner(propertyId);
        Invoice invoice = lockEditable(propertyId, invoiceId);
        InvoiceItem target = invoice.getItems().stream().filter(i -> i.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_ITEM_NOT_FOUND));
        if (target.getItemType() != InvoiceItemType.OTHER) {
            throw new AppException(ErrorCode.INVOICE_ITEM_NOT_ADJUSTMENT);
        }
        invoice.getItems().remove(target);
        recalculate(invoice);
        return toDetail(invoiceRepository.save(invoice));
    }

    private Invoice lockEditable(UUID propertyId, UUID invoiceId) {
        Invoice invoice = invoiceRepository.findForUpdate(invoiceId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        if (invoice.getStatus() != InvoiceStatus.ISSUED && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID) {
            throw new AppException(ErrorCode.INVOICE_NOT_EDITABLE);
        }
        // Giao dịch online đang chờ mang số tiền cũ; đổi tổng lúc này sẽ làm IPN không khớp
        if (transactionRepository.existsByInvoiceIdAndStatusAndCreatedAtAfter(invoiceId,
                PaymentTransactionStatus.PENDING, Instant.now().minus(AppConstant.PENDING_PAYMENT_TTL))) {
            throw new AppException(ErrorCode.INVOICE_HAS_PENDING_PAYMENT);
        }
        return invoice;
    }

    private void recalculate(Invoice invoice) {
        BigDecimal total = invoice.getItems().stream().map(InvoiceItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(invoice.getPaidAmount()) < 0) {
            throw new AppException(ErrorCode.INVOICE_TOTAL_BELOW_PAID);
        }
        invoice.setTotalAmount(total);
        invoice.refreshStatus();
    }

    private InvoiceResponse toDetail(Invoice invoice) {
        InvoiceResponse response = invoiceMapper.toResponse(invoice);
        response.getItems().sort(Comparator.comparing(InvoiceItemResponse::getItemType));
        return response;
    }

    public void delete(UUID propertyId, UUID invoiceId) {
        accessService.requireOwner(propertyId);
        Invoice invoice = find(propertyId, invoiceId);
        if (invoice.getPaidAmount().signum() > 0) {
            throw new AppException(ErrorCode.INVOICE_HAS_PAYMENTS);
        }
        if (transactionRepository.existsByInvoiceIdAndStatusAndCreatedAtAfter(invoiceId,
                PaymentTransactionStatus.PENDING, Instant.now().minus(AppConstant.PENDING_PAYMENT_TTL))) {
            throw new AppException(ErrorCode.INVOICE_HAS_PENDING_PAYMENT);
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setDeleted(true);
        invoice.getItems().forEach(i -> i.setDeleted(true));
        invoiceRepository.save(invoice);
    }


    private Invoice find(UUID propertyId, UUID invoiceId) {
        return invoiceRepository.findByIdAndPropertyIdAndDeletedFalse(invoiceId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
    }

    /**
     * Tiền phòng. Hợp đồng bắt đầu giữa tháng thì tính theo số ngày ở thực tế / số ngày trong tháng.
     * Chỉ tính theo ngày bắt đầu: ngày kết thúc dự kiến không dùng để cắt tiền phòng vì hợp đồng hết hạn
     * mà khách vẫn ở thì vẫn thu đủ cho đến khi chủ trọ kết thúc hợp đồng.
     */
    private static InvoiceItem rentItem(Contract contract, LocalDate period, LocalDate periodEnd, String monthLabel) {
        LocalDate from = contract.getStartDate().isAfter(period) ? contract.getStartDate() : period;
        // Chỉ hợp đồng ĐÃ kết thúc mới cắt theo ngày rời đi (ngày thực tế); ngày kết thúc dự kiến của hợp đồng
        // đang hiệu lực không dùng để cắt tiền phòng
        LocalDate to = contract.getStatus() == ContractStatus.ENDED && contract.getEndDate() != null
                && contract.getEndDate().isBefore(periodEnd) ? contract.getEndDate() : periodEnd;
        if (from.equals(period) && to.equals(periodEnd)) {
            return item(InvoiceItemType.RENT, "Tiền phòng tháng " + monthLabel, BigDecimal.ONE, contract.getMonthlyRent());
        }
        long days = Math.max(1, ChronoUnit.DAYS.between(from, to) + 1);
        int daysInMonth = period.lengthOfMonth();
        BigDecimal prorated = contract.getMonthlyRent().multiply(BigDecimal.valueOf(days))
                .divide(BigDecimal.valueOf(daysInMonth), 0, RoundingMode.HALF_UP);
        DateTimeFormatter dm = DateTimeFormatter.ofPattern("dd/MM");
        String desc = "Tiền phòng tháng %s (%d/%d ngày, từ %s đến %s)".formatted(monthLabel, days, daysInMonth,
                from.format(dm), to.format(dm));
        return item(InvoiceItemType.RENT, desc, BigDecimal.ONE, prorated);
    }

    /** Tách mức tiêu thụ theo từng bậc giá; mỗi bậc có tiêu thụ > 0 là một dòng hóa đơn. */
    static List<InvoiceItem> tieredItems(InvoiceItemType type, String baseDesc, BigDecimal consumption,
                                         ServicePrice service) {
        List<InvoiceItem> items = new ArrayList<>();
        List<PriceTier> tiers = service.getTiers();
        BigDecimal remaining = consumption;
        BigDecimal lower = BigDecimal.ZERO;
        for (int i = 0; i < tiers.size() && remaining.signum() > 0; i++) {
            PriceTier tier = tiers.get(i);
            BigDecimal upper = tier.getUpTo();
            BigDecimal width = upper == null ? remaining : remaining.min(upper.subtract(lower));
            if (width.signum() > 0) {
                String range = upper == null ? "trên " + plain(lower) : plain(lower) + "–" + plain(upper);
                String desc = "%s - bậc %d (%s %s)".formatted(baseDesc, i + 1, range, service.getUnit());
                items.add(item(type, desc.length() > 200 ? desc.substring(0, 200) : desc, width, tier.getUnitPrice()));
                remaining = remaining.subtract(width);
            }
            if (upper != null) lower = upper;
        }
        return items;
    }

    private static InvoiceItem item(InvoiceItemType type, String description, BigDecimal quantity, BigDecimal unitPrice) {
        InvoiceItem item = new InvoiceItem();
        item.setItemType(type);
        item.setDescription(description);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setAmount(quantity.multiply(unitPrice).setScale(0, RoundingMode.HALF_UP));
        return item;
    }

    private static SkippedRoom skip(Room room, SkipReason reason, String detail) {
        return SkippedRoom.builder().roomId(room.getId()).roomCode(room.getCode())
                .reason(reason).detail(detail).build();
    }

    private static String plain(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}