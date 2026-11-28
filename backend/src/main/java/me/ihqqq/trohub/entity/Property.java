package me.ihqqq.trohub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "property")
public class Property extends BaseEntity {

    @Column(nullable = false, length = 150)
    String name;

    String address;
    String description;

    @Column(name = "bank_name", length = 100)
    String bankName;

    @Column(name = "bank_account_number", length = 30)
    String bankAccountNumber;

    @Column(name = "bank_account_holder", length = 100)
    String bankAccountHolder;
}