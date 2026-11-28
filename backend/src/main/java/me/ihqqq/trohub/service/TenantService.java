package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.TenantRequest;
import me.ihqqq.trohub.dto.response.PageResponse;
import me.ihqqq.trohub.dto.response.TenantResponse;
import me.ihqqq.trohub.entity.Tenant;
import me.ihqqq.trohub.enums.ContractStatus;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.mapper.TenantMapper;
import me.ihqqq.trohub.repository.ContractRepository;
import me.ihqqq.trohub.repository.TenantRepository;
import me.ihqqq.trohub.util.PagingUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class TenantService {

    TenantRepository tenantRepository;
    ContractRepository contractRepository;
    TenantMapper tenantMapper;
    PropertyAccessService accessService;

    @Transactional(readOnly = true)
    public PageResponse<TenantResponse> list(UUID propertyId, int page, int size) {
        accessService.requireMember(propertyId);
        var pageable = PagingUtils.pageable(page, size, Sort.by("fullName").ascending().and(Sort.by("id")));
        return PageResponse.from(tenantRepository.findAllByPropertyIdAndDeletedFalse(propertyId, pageable),
                tenantMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TenantResponse get(UUID propertyId, UUID tenantId) {
        accessService.requireMember(propertyId);
        return tenantMapper.toResponse(find(propertyId, tenantId));
    }

    public TenantResponse create(UUID propertyId, TenantRequest request) {
        var access = accessService.requireMember(propertyId);
        Tenant tenant = tenantMapper.toTenant(request);
        tenant.setProperty(access.property());
        return tenantMapper.toResponse(tenantRepository.save(tenant));
    }

    public TenantResponse update(UUID propertyId, UUID tenantId, TenantRequest request) {
        accessService.requireMember(propertyId);
        Tenant tenant = find(propertyId, tenantId);
        tenantMapper.update(tenant, request);
        return tenantMapper.toResponse(tenantRepository.save(tenant));
    }

    public void delete(UUID propertyId, UUID tenantId) {
        accessService.requireOwner(propertyId);
        Tenant tenant = find(propertyId, tenantId);
        if (contractRepository.existsByTenantIdAndStatusAndDeletedFalse(tenantId, ContractStatus.ACTIVE)) {
            throw new AppException(ErrorCode.TENANT_HAS_ACTIVE_CONTRACT);
        }
        tenant.setDeleted(true);
        tenantRepository.save(tenant);
    }

    private Tenant find(UUID propertyId, UUID tenantId) {
        return tenantRepository.findByIdAndPropertyIdAndDeletedFalse(tenantId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));
    }
}