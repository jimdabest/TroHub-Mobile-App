package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.PriceTierRequest;
import me.ihqqq.trohub.dto.request.ServicePriceRequest;
import me.ihqqq.trohub.dto.response.ServicePriceResponse;
import me.ihqqq.trohub.entity.PriceTier;
import me.ihqqq.trohub.entity.ServicePrice;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.mapper.ServicePriceMapper;
import me.ihqqq.trohub.repository.ServicePriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ServicePriceService {

    ServicePriceRepository servicePriceRepository;
    ServicePriceMapper servicePriceMapper;
    PropertyAccessService accessService;

    @Transactional(readOnly = true)
    public List<ServicePriceResponse> list(UUID propertyId) {
        accessService.requireMember(propertyId);
        return servicePriceRepository.findAllByPropertyIdAndDeletedFalseOrderByNameAsc(propertyId).stream()
                .map(servicePriceMapper::toResponse).toList();
    }

    public ServicePriceResponse create(UUID propertyId, ServicePriceRequest request) {
        var access = accessService.requireOwner(propertyId);
        ServicePrice servicePrice = servicePriceMapper.toServicePrice(request);
        servicePrice.setProperty(access.property());
        applyTiers(servicePrice, request);
        return servicePriceMapper.toResponse(servicePriceRepository.save(servicePrice));
    }

    public ServicePriceResponse update(UUID propertyId, UUID serviceId, ServicePriceRequest request) {
        accessService.requireOwner(propertyId);
        ServicePrice servicePrice = find(propertyId, serviceId);
        servicePriceMapper.update(servicePrice, request);
        applyTiers(servicePrice, request);
        servicePrice.setUpdatedAt(Instant.now());   // đổi riêng bậc giá không làm entity dirty, cần cho đồng bộ
        return servicePriceMapper.toResponse(servicePriceRepository.save(servicePrice));
    }

    public void delete(UUID propertyId, UUID serviceId) {
        accessService.requireOwner(propertyId);
        ServicePrice servicePrice = find(propertyId, serviceId);
        servicePrice.setDeleted(true);
        servicePriceRepository.save(servicePrice);
    }

    static final int MAX_TIERS = 10;

    /**
     * tiers == null: giữ nguyên (khi cập nhật). Rỗng: bỏ bậc thang. Có phần tử: kiểm tra rồi thay toàn bộ.
     * Dịch vụ không phải điện/nước không có bậc giá.
     */
    private void applyTiers(ServicePrice servicePrice, ServicePriceRequest request) {
        List<PriceTierRequest> requested = request.getTiers();
        if (!MeterReadingService.METERED_TYPES.contains(servicePrice.getType())) {
            if (requested != null && !requested.isEmpty()) {
                throw new AppException(ErrorCode.SERVICE_TIER_NOT_ALLOWED);
            }
            servicePrice.getTiers().clear();
            return;
        }
        if (requested == null) return;

        List<PriceTier> tiers = new ArrayList<>();
        BigDecimal previousUpTo = BigDecimal.ZERO;
        if (requested.size() > MAX_TIERS) throw new AppException(ErrorCode.SERVICE_TIER_INVALID);
        for (int i = 0; i < requested.size(); i++) {
            PriceTierRequest t = requested.get(i);
            boolean last = i == requested.size() - 1;
            if (last) {
                if (t.getUpTo() != null) throw new AppException(ErrorCode.SERVICE_TIER_INVALID);
            } else {
                if (t.getUpTo() == null || t.getUpTo().compareTo(previousUpTo) <= 0) {
                    throw new AppException(ErrorCode.SERVICE_TIER_INVALID);
                }
                previousUpTo = t.getUpTo();
            }
            tiers.add(new PriceTier(t.getUpTo(), t.getUnitPrice()));
        }
        servicePrice.getTiers().clear();
        servicePrice.getTiers().addAll(tiers);
    }

    private ServicePrice find(UUID propertyId, UUID serviceId) {
        return servicePriceRepository.findByIdAndPropertyIdAndDeletedFalse(serviceId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));
    }
}