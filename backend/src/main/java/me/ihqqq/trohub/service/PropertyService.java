package me.ihqqq.trohub.service;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.MemberRequest;
import me.ihqqq.trohub.dto.request.PropertyRequest;
import me.ihqqq.trohub.dto.response.MemberResponse;
import me.ihqqq.trohub.dto.response.PropertyResponse;
import me.ihqqq.trohub.entity.Property;
import me.ihqqq.trohub.entity.PropertyMember;
import me.ihqqq.trohub.entity.User;
import me.ihqqq.trohub.enums.PropertyRole;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.mapper.PropertyMapper;
import me.ihqqq.trohub.repository.PropertyMemberRepository;
import me.ihqqq.trohub.repository.PropertyRepository;
import me.ihqqq.trohub.repository.UserRepository;
import me.ihqqq.trohub.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PropertyService {

    PropertyRepository propertyRepository;
    PropertyMemberRepository propertyMemberRepository;
    UserRepository userRepository;
    PropertyMapper propertyMapper;
    PropertyAccessService accessService;

    /** Nguoi tao tro thanh OWNER cua nha tro do. */
    public PropertyResponse create(PropertyRequest request) {
        User user = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        Property property = propertyRepository.save(propertyMapper.toProperty(request));

        PropertyMember owner = new PropertyMember();
        owner.setProperty(property);
        owner.setUser(user);
        owner.setRole(PropertyRole.OWNER);
        propertyMemberRepository.save(owner);

        return toResponse(property, PropertyRole.OWNER);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> listMine() {
        return propertyMemberRepository.findAllActiveByUserId(SecurityUtils.currentUserId()).stream()
                .map(m -> toResponse(m.getProperty(), m.getRole()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PropertyResponse get(UUID propertyId) {
        var access = accessService.requireMember(propertyId);
        return toResponse(access.property(), access.role());
    }

    public PropertyResponse update(UUID propertyId, PropertyRequest request) {
        var access = accessService.requireOwner(propertyId);
        propertyMapper.update(access.property(), request);
        return toResponse(propertyRepository.save(access.property()), access.role());
    }

    public void delete(UUID propertyId) {
        var access = accessService.requireOwner(propertyId);
        access.property().setDeleted(true);
        propertyRepository.save(access.property());
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers(UUID propertyId) {
        accessService.requireMember(propertyId);
        return propertyMemberRepository.findAllWithUser(propertyId).stream().map(this::toMemberResponse).toList();
    }

    public MemberResponse addMember(UUID propertyId, MemberRequest request) {
        var access = accessService.requireOwner(propertyId);
        if (request.getRole() != PropertyRole.MANAGER) {
            throw new AppException(ErrorCode.MEMBER_ROLE_INVALID);
        }

        User user = userRepository.findByUsername(request.getUsername().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (propertyMemberRepository.existsByPropertyIdAndUserId(propertyId, user.getId())) {
            throw new AppException(ErrorCode.MEMBER_EXISTED);
        }

        PropertyMember member = new PropertyMember();
        member.setProperty(access.property());
        member.setUser(user);
        member.setRole(PropertyRole.MANAGER);
        return toMemberResponse(propertyMemberRepository.save(member));
    }

    public void removeMember(UUID propertyId, UUID userId) {
        accessService.requireOwner(propertyId);
        PropertyMember member = propertyMemberRepository.findByPropertyIdAndUserId(propertyId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.getRole() == PropertyRole.OWNER) {
            throw new AppException(ErrorCode.CANNOT_REMOVE_OWNER);
        }
        propertyMemberRepository.delete(member);
    }

    private PropertyResponse toResponse(Property property, PropertyRole role) {
        PropertyResponse response = propertyMapper.toResponse(property);
        response.setMyRole(role);
        return response;
    }

    private MemberResponse toMemberResponse(PropertyMember member) {
        return MemberResponse.builder()
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .fullName(member.getUser().getFullName())
                .role(member.getRole())
                .build();
    }
}