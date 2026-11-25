package me.ihqqq.trohub.enums;

import java.util.EnumSet;
import java.util.Set;

public enum InvoiceStatusFilter {
    ALL, UNPAID, PAID;

    public Set<InvoiceStatus> tenantVisibleStatuses() {
        return switch (this) {
            case ALL -> EnumSet.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.PAID);
            case UNPAID -> EnumSet.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID);
            case PAID -> EnumSet.of(InvoiceStatus.PAID);
        };
    }

    public Set<InvoiceStatus> statuses() {
        return switch (this) {
            case ALL -> EnumSet.of(InvoiceStatus.DRAFT, InvoiceStatus.ISSUED,
                    InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.PAID);
            case UNPAID -> EnumSet.of(InvoiceStatus.DRAFT, InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID);
            case PAID -> EnumSet.of(InvoiceStatus.PAID);
        };
    }
}