package me.ihqqq.trohub.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.entity.Property;
import me.ihqqq.trohub.entity.Room;
import me.ihqqq.trohub.entity.Tenant;
import me.ihqqq.trohub.enums.ContractStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "contract")
public class Contract extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    Property property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id")
    Tenant tenant;

    @Column(name = "start_date", nullable = false)
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    @Column(name = "monthly_rent", nullable = false)
    BigDecimal monthlyRent;

    @Column(nullable = false)
    BigDecimal deposit;

    @Column(nullable = false)
    int occupants = 1;

    /** Quyết toán cọc khi kết thúc hợp đồng; cả hai null = chưa quyết toán. */
    @Column(name = "deposit_refunded")
    BigDecimal depositRefunded;

    @Column(name = "deposit_deducted")
    BigDecimal depositDeducted;

    @Column(name = "deposit_note", length = 500)
    String depositNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    ContractStatus status;

    String note;
}