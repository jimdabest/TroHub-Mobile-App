package me.ihqqq.trohub.enums;

public enum PaymentTransactionStatus {
    PENDING,
    /** Quá hạn chờ thanh toán. IPN thành công đến trễ vẫn được ghi nhận. */
    EXPIRED,
    SUCCESS,
    FAILED,
    /** Cổng đã thu tiền thành công nhưng không ghi nhận được vào hóa đơn: cần chủ trọ đối soát/hoàn tiền. */
    SUCCESS_UNAPPLIED,
    /** Chủ trọ đã xử lý xong một giao dịch SUCCESS_UNAPPLIED (đã hoàn tiền hoặc ghi nhận thủ công). */
    RECONCILED
}