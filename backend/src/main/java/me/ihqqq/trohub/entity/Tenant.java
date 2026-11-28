package me.ihqqq.trohub.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.util.PhoneUtils;

import java.time.Instant;
import java.util.UUID;
import me.ihqqq.trohub.entity.BaseEntity;
import me.ihqqq.trohub.entity.Property;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tenant")
public class Tenant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    Property property;

    @Column(name = "full_name", nullable = false, length = 100)
    String fullName;

    String phone;

    @Column(name = "id_number")
    String idNumber;

    String note;

    @Column(name = "phone_normalized", length = 20)
    String phoneNormalized;

    /** Tài khoản đã liên kết với hồ sơ này (đăng nhập tenant portal). null = chưa liên kết. */
    @Column(name = "user_id")
    UUID userId;

    /** SHA-256 của mã mời đang chờ; mã gốc không lưu. */
    @Column(name = "invite_code_hash", length = 64)
    String inviteCodeHash;

    @Column(name = "invite_expires_at")
    Instant inviteExpiresAt;

    public void setPhone(String phone) {
        this.phone = phone;
        this.phoneNormalized = PhoneUtils.normalize(phone);
    }
}