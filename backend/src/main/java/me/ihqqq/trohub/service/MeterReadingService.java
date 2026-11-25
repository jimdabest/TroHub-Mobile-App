package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.MeterReadingRequest;
import me.ihqqq.trohub.dto.response.MeterReadingResponse;
import me.ihqqq.trohub.entity.MeterReading;
import me.ihqqq.trohub.entity.Room;
import me.ihqqq.trohub.entity.ServicePrice;
import me.ihqqq.trohub.enums.ServiceType;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.mapper.MeterReadingMapper;
import me.ihqqq.trohub.repository.MeterReadingRepository;
import me.ihqqq.trohub.repository.RoomRepository;
import me.ihqqq.trohub.repository.ServicePriceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class MeterReadingService {

    static final Set<ServiceType> METERED_TYPES = Set.of(ServiceType.ELECTRIC, ServiceType.WATER);

    MeterReadingRepository meterReadingRepository;
    RoomRepository roomRepository;
    ServicePriceRepository servicePriceRepository;
    MeterReadingMapper meterReadingMapper;
    PropertyAccessService accessService;

    public MeterReadingResponse create(UUID propertyId, MeterReadingRequest request) {
        var access = accessService.requireMember(propertyId);

        Room room = findRoom(propertyId, request.getRoomId());
        ServicePrice service = servicePriceRepository
                .findByIdAndPropertyIdAndDeletedFalse(request.getServicePriceId(), propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));
        if (!METERED_TYPES.contains(service.getType())) {
            throw new AppException(ErrorCode.METER_SERVICE_TYPE_INVALID);
        }

        LocalDate period = request.getPeriod().withDayOfMonth(1);

        if (meterReadingRepository.existsByRoomIdAndServicePriceIdAndPeriodAndDeletedFalse(
                room.getId(), service.getId(), period)) {
            throw new AppException(ErrorCode.METER_READING_EXISTED);
        }

        BigDecimal previous = request.getPreviousValue() != null
                ? request.getPreviousValue()
                : meterReadingRepository
                .findFirstByRoomIdAndServicePriceIdAndPeriodLessThanAndDeletedFalseOrderByPeriodDesc(
                        room.getId(), service.getId(), period)
                .map(MeterReading::getCurrentValue)
                .orElse(BigDecimal.ZERO);

        if (request.getCurrentValue().compareTo(previous) < 0) {
            throw new AppException(ErrorCode.METER_VALUE_INVALID);
        }

        MeterReading reading = new MeterReading();
        reading.setProperty(access.property());
        reading.setRoom(room);
        reading.setServicePrice(service);
        reading.setPeriod(period);
        reading.setPreviousValue(previous);
        reading.setCurrentValue(request.getCurrentValue());
        reading.setReadAt(Instant.now());

        try {
            return meterReadingMapper.toResponse(meterReadingRepository.saveAndFlush(reading));
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.METER_READING_EXISTED);
        }
    }

    @Transactional(readOnly = true)
    public List<MeterReadingResponse> latest(UUID propertyId, UUID roomId) {
        accessService.requireMember(propertyId);
        Room room = findRoom(propertyId, roomId);

        return servicePriceRepository.findAllByPropertyIdAndDeletedFalseOrderByNameAsc(propertyId).stream()
                .filter(s -> s.isActive() && METERED_TYPES.contains(s.getType()))
                .map(s -> meterReadingRepository
                        .findFirstByRoomIdAndServicePriceIdAndDeletedFalseOrderByPeriodDesc(room.getId(), s.getId()))
                .flatMap(java.util.Optional::stream)
                .map(meterReadingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MeterReadingResponse> listByPeriod(UUID propertyId, LocalDate period) {
        accessService.requireMember(propertyId);
        return meterReadingRepository.findAllByPropertyAndPeriod(propertyId, period.withDayOfMonth(1)).stream()
                .map(meterReadingMapper::toResponse)
                .toList();
    }

    private Room findRoom(UUID propertyId, UUID roomId) {
        return roomRepository.findByIdAndPropertyIdAndDeletedFalse(roomId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
    }
}