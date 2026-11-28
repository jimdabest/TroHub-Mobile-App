package me.ihqqq.trohub.enums;

public enum IpnResult {
    INVALID_SIGNATURE, NOT_FOUND, AMOUNT_MISMATCH,
    ALREADY_PROCESSED,
    IGNORED_PENDING,
    SETTLED;

    public boolean isAcknowledged() {
        return this == ALREADY_PROCESSED || this == IGNORED_PENDING || this == SETTLED;
    }
}