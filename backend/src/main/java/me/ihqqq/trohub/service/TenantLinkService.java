package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.response.LinkedTenantResponse;
import me.ihqqq.trohub.dto.response.TenantInviteResponse;
import me.ihqqq.trohub.dto.response.TenantResponse;
import me.ihqqq.trohub.entity.Tenant;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.mapper.TenantMapper;
import me.ihqqq.trohub.repository.TenantRepository;
import me.ihqqq.trohub.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * Liên kết tài khoản với hồ sơ người thuê bằng mã mời do chủ trọ cấp. Thay cho việc khớp theo số điện thoại:
 * số điện thoại tài khoản không được xác minh nên ai cũng khai được số của người khác.
 * Mã: 8 ký tự ngẫu nhiên (bảng chữ 32 ký tự, không có 0/O/1/I), hết hạn sau 7 ngày, chỉ lưu bản băm SHA-256,
 * dùng một lần. Nhập sai bị giới hạn theo tài khoản.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class TenantLinkService {

    static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    static final int CODE_LENGTH = 8;
    static final Duration INVITE_TTL = Duration.ofDays(7);
    static final SecureRandom RANDOM = new SecureRandom();

    TenantRepository tenantRepository;
    TenantMapper tenantMapper;
    PropertyAccessService accessService;
    LinkAttemptService linkAttemptService;

    /** Chủ trọ/quản lý tạo mã mời cho một hồ sơ chưa liên kết. Tạo lại thì mã cũ mất hiệu lực. */
    public TenantInviteResponse invite(UUID propertyId, UUID tenantId) {
        accessService.requireMember(propertyId);
        Tenant tenant = find(propertyId, tenantId);
        if (tenant.getUserId() != null) {
            throw new AppException(ErrorCode.TENANT_ALREADY_LINKED);
        }
        String code = generateCode();
        Instant expiresAt = Instant.now().plus(INVITE_TTL);
        tenant.setInviteCodeHash(hash(code));
        tenant.setInviteExpiresAt(expiresAt);
        tenantRepository.save(tenant);
        return TenantInviteResponse.builder().code(display(code)).expiresAt(expiresAt).build();
    }

    /** Gỡ liên kết (khách đổi tài khoản, hoặc liên kết nhầm) và hủy mã đang chờ. */
    public TenantResponse unlink(UUID propertyId, UUID tenantId) {
        accessService.requireMember(propertyId);
        Tenant tenant = find(propertyId, tenantId);
        tenant.setUserId(null);
        tenant.setInviteCodeHash(null);
        tenant.setInviteExpiresAt(null);
        return tenantMapper.toResponse(tenantRepository.save(tenant));
    }

    /** Khách nhập mã trong app để liên kết tài khoản đang đăng nhập với hồ sơ của mình. */
    public LinkedTenantResponse link(String rawCode) {
        UUID userId = SecurityUtils.currentUserId();
        linkAttemptService.checkAllowed(userId);

        Tenant tenant = tenantRepository.findForLink(hash(normalize(rawCode)))
                .filter(t -> t.getUserId() == null && t.getInviteExpiresAt() != null
                        && t.getInviteExpiresAt().isAfter(Instant.now()))
                .orElse(null);
        if (tenant == null) {
            // Một lỗi chung cho cả "sai mã" và "hết hạn" để không lộ mã nào tồn tại
            linkAttemptService.recordFailure(userId);
            throw new AppException(ErrorCode.INVITE_CODE_INVALID);
        }
        linkAttemptService.recordSuccess(userId);

        tenant.setUserId(userId);
        tenant.setInviteCodeHash(null);
        tenant.setInviteExpiresAt(null);
        tenantRepository.save(tenant);
        return LinkedTenantResponse.builder()
                .tenantId(tenant.getId())
                .fullName(tenant.getFullName())
                .propertyId(tenant.getProperty().getId())
                .propertyName(tenant.getProperty().getName())
                .build();
    }

    private Tenant find(UUID propertyId, UUID tenantId) {
        return tenantRepository.findByIdAndPropertyIdAndDeletedFalse(tenantId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));
    }

    static String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    /** Khách có thể gõ chữ thường, có dấu gạch hoặc khoảng trắng: bỏ hết rồi viết hoa. */
    static String normalize(String raw) {
        return raw == null ? "" : raw.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    static String display(String code) {
        return code.substring(0, 4) + "-" + code.substring(4);
    }

    static String hash(String normalizedCode) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(normalizedCode.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}