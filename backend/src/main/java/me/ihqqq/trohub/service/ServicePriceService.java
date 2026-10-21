package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.ServicePriceRequest;
import me.ihqqq.trohub.dto.response.ServicePriceResponse;
import me.ihqqq.trohub.entity.ServicePrice;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.mapper.ServicePriceMapper;
import me.ihqqq.trohub.repository.ServicePriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return servicePriceMapper.toResponse(servicePriceRepository.save(servicePrice));
    }

    public ServicePriceResponse update(UUID propertyId, UUID serviceId, ServicePriceRequest request) {
        accessService.requireOwner(propertyId);
        ServicePrice servicePrice = find(propertyId, serviceId);
        servicePriceMapper.update(servicePrice, request);
        return servicePriceMapper.toResponse(servicePriceRepository.save(servicePrice));
    }

    public void delete(UUID propertyId, UUID serviceId) {
        accessService.requireOwner(propertyId);
        ServicePrice servicePrice = find(propertyId, serviceId);
        servicePrice.setDeleted(true);
        servicePriceRepository.save(servicePrice);
    }

    private ServicePrice find(UUID propertyId, UUID serviceId) {
        return servicePriceRepository.findByIdAndPropertyIdAndDeletedFalse(serviceId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));
    }
}