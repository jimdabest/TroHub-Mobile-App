package me.ihqqq.trohub.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
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
}