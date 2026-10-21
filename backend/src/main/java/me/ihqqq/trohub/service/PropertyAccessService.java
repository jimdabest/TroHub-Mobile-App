package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.entity.Property;
import me.ihqqq.trohub.entity.PropertyMember;
import me.ihqqq.trohub.enums.PropertyRole;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.repository.PropertyMemberRepository;
import me.ihqqq.trohub.repository.PropertyRepository;
import me.ihqqq.trohub.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PropertyAccessService {

    public record Access(Property property, PropertyRole role) {}

    PropertyRepository propertyRepository;
    PropertyMemberRepository propertyMemberRepository;

    @Transactional(readOnly = true)
    public Access requireMember(UUID propertyId) {
        Property property = propertyRepository.findByIdAndDeletedFalse(propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.PROPERTY_NOT_FOUND));
        PropertyMember member = propertyMemberRepository
                .findByPropertyIdAndUserId(propertyId, SecurityUtils.currentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.PROPERTY_NOT_FOUND));
        return new Access(property, member.getRole());
    }

    @Transactional(readOnly = true)
    public Access requireOwner(UUID propertyId) {
        Access access = requireMember(propertyId);
        if (access.role() != PropertyRole.OWNER) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return access;
    }
}