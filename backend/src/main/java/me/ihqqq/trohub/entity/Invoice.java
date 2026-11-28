package me.ihqqq.trohub.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "invoice")
public class Invoice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    Property property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    Contract contract;

    @Column(nullable = false)
    LocalDate period;

    @Column(name = "issue_date", nullable = false)
    LocalDate issueDate;

    @Column(name = "due_date")
    LocalDate dueDate;

    @Column(name = "total_amount", nullable = false)
    BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false)
    BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    InvoiceStatus status;

    String note;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    List<InvoiceItem> items = new ArrayList<>();

    /** Tính lại trạng thái theo tổng tiền và số đã thu. DRAFT/CANCELLED giữ nguyên. */
    public void refreshStatus() {
        if (status == InvoiceStatus.DRAFT || status == InvoiceStatus.CANCELLED) return;
        if (paidAmount.compareTo(totalAmount) >= 0) {
            status = InvoiceStatus.PAID;
        } else if (paidAmount.signum() > 0) {
            status = InvoiceStatus.PARTIALLY_PAID;
        } else {
            status = InvoiceStatus.ISSUED;
        }
    }

    /** Quá hạn = chưa thanh toán đủ và đã qua hạn thanh toán. Tính theo ngày, không lưu trạng thái nên không bao giờ cũ. */
    public boolean isOverdue(LocalDate today) {
        return (status == InvoiceStatus.ISSUED || status == InvoiceStatus.PARTIALLY_PAID)
                && dueDate != null && dueDate.isBefore(today);
    }

    public void addItem(InvoiceItem item) {
        item.setInvoice(this);
        items.add(item);
    }
}