package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.constant.AppConstant;
import me.ihqqq.trohub.dto.request.InvoiceGenerationRequest;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

        Map<UUID, Contract> activeByRoom = contractRepository
                .findAllWithRoomByPropertyAndStatus(propertyId, ContractStatus.ACTIVE).stream()
                .filter(c -> !c.getStartDate().isAfter(periodEnd))
                .collect(Collectors.toMap(c -> c.getRoom().getId(), c -> c, (a, b) -> a));

        List<ServicePrice> activeServices = servicePriceRepository
                .findAllByPropertyIdAndDeletedFalseOrderByNameAsc(propertyId).stream()
                .filter(ServicePrice::isActive).toList();
        List<ServicePrice> meteredServices = activeServices.stream()
                .filter(s -> MeterReadingService.METERED_TYPES.contains(s.getType())).toList();
        List<ServicePrice> fixedServices = activeServices.stream()
                .filter(s -> s.getType() == ServiceType.FIXED).toList();

        Map<UUID, Map<UUID, MeterReading>> readingsByRoom = new HashMap<>();
        for (MeterReading m : meterReadingRepository.findAllByPropertyAndPeriod(propertyId, period)) {
            readingsByRoom.computeIfAbsent(m.getRoom().getId(), k -> new HashMap<>())
                    .put(m.getServicePrice().getId(), m);
        }

        Set<UUID> alreadyInvoiced = new HashSet<>(invoiceRepository.findInvoicedRoomIds(propertyId, period));

        List<Invoice> toSave = new ArrayList<>();
        List<SkippedRoom> skipped = new ArrayList<>();

        for (Room room : roomsById.values()) {
            boolean targeted = requested == null ? activeByRoom.containsKey(room.getId())
                    : requested.contains(room.getId());
            if (!targeted) continue;

            if (alreadyInvoiced.contains(room.getId())) {
                skipped.add(skip(room, SkipReason.ALREADY_INVOICED, "Phòng đã có hóa đơn kỳ " + monthLabel));
                continue;
            }
            Contract contract = activeByRoom.get(room.getId());
            if (contract == null) {
                skipped.add(skip(room, SkipReason.NO_ACTIVE_CONTRACT, "Phòng không có hợp đồng đang hiệu lực"));
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

            invoice.addItem(item(InvoiceItemType.RENT, "Tiền phòng tháng " + monthLabel,
                    BigDecimal.ONE, contract.getMonthlyRent()));

            for (ServicePrice service : meteredServices) {
                MeterReading reading = roomReadings.get(service.getId());
                BigDecimal consumption = reading.getCurrentValue().subtract(reading.getPreviousValue());
                String desc = "%s tháng %s (%s → %s %s)".formatted(service.getName(), monthLabel,
                        plain(reading.getPreviousValue()), plain(reading.getCurrentValue()), service.getUnit());
                invoice.addItem(item(
                        service.getType() == ServiceType.ELECTRIC ? InvoiceItemType.ELECTRIC : InvoiceItemType.WATER,
                        desc, consumption, service.getUnitPrice()));
            }

            for (ServicePrice service : fixedServices) {
                invoice.addItem(item(InvoiceItemType.SERVICE, service.getName(), BigDecimal.ONE, service.getUnitPrice()));
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
        return invoices.stream().map(invoiceMapper::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse get(UUID propertyId, UUID invoiceId) {
        accessService.requireMember(propertyId);
        InvoiceResponse response = invoiceMapper.toResponse(find(propertyId, invoiceId));
        response.getItems().sort(Comparator.comparing(InvoiceItemResponse::getItemType));
        return response;
    }

    public void delete(UUID propertyId, UUID invoiceId) {
        accessService.requireOwner(propertyId);
        Invoice invoice = find(propertyId, invoiceId);
        if (invoice.getPaidAmount().signum() > 0) {
            throw new AppException(ErrorCode.INVOICE_HAS_PAYMENTS);
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