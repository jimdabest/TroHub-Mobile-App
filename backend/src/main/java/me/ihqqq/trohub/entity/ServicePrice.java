package me.ihqqq.trohub.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.entity.BaseEntity;
import me.ihqqq.trohub.enums.ServiceType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "service_price")
public class ServicePrice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    Property property;

    @Column(nullable = false, length = 100)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    ServiceType type;

    @Column(nullable = false, length = 20)
    String unit;

    @Column(name = "unit_price", nullable = false)
    BigDecimal unitPrice;

    @Column(nullable = false)
    boolean active = true;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "service_price_tier", joinColumns = @JoinColumn(name = "service_price_id"))
    @OrderColumn(name = "tier_order")
    List<PriceTier> tiers = new ArrayList<>();
}