package me.ihqqq.trohub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceTier {
    @Column(name = "up_to")
    BigDecimal upTo;

    @Column(name = "unit_price", nullable = false)
    BigDecimal unitPrice;
}