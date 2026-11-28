package me.ihqqq.trohub.enums;

public enum PaymentMethod {
    CASH, BANK_TRANSFER, VNPAY, MOMO;

    public boolean isManual() {
        return this == CASH || this == BANK_TRANSFER;
    }
}