package me.ihqqq.trohub.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.InvoiceItemType;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "invoice_item")
public class InvoiceItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id")
    Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    InvoiceItemType itemType;

    @Column(nullable = false, length = 200)
    String description;

    @Column(nullable = false)
    BigDecimal quantity;

    @Column(name = "unit_price", nullable = false)
    BigDecimal unitPrice;

    @Column(nullable = false)
    BigDecimal amount;
}