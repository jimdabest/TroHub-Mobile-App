package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.trohub.configuration.VnPayProperties;
import me.ihqqq.trohub.constant.AppConstant;
import me.ihqqq.trohub.dto.request.OnlinePaymentRequest;
import me.ihqqq.trohub.dto.response.OnlinePaymentResponse;
import me.ihqqq.trohub.entity.Invoice;
import me.ihqqq.trohub.entity.PaymentTransaction;
import me.ihqqq.trohub.enums.InvoiceStatus;
import me.ihqqq.trohub.enums.PaymentMethod;
import me.ihqqq.trohub.enums.PaymentProvider;
import me.ihqqq.trohub.enums.PaymentTransactionStatus;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.repository.InvoiceRepository;
import me.ihqqq.trohub.repository.PaymentTransactionRepository;
import me.ihqqq.trohub.util.VnPayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class VnPayService {

    static final DateTimeFormatter VNP_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    VnPayProperties props;
    InvoiceRepository invoiceRepository;
    PaymentTransactionRepository transactionRepository;
    PaymentService paymentService;
    PropertyAccessService accessService;


    public OnlinePaymentResponse createPaymentUrl(UUID propertyId, UUID invoiceId,
                                                  OnlinePaymentRequest request, String clientIp) {
        accessService.requireMember(propertyId);
        return buildPaymentUrl(propertyId, invoiceId, request, clientIp);
    }

    public OnlinePaymentResponse createPaymentUrlForTenant(UUID propertyId, UUID invoiceId,
                                                           OnlinePaymentRequest request, String clientIp) {
        return buildPaymentUrl(propertyId, invoiceId, request, clientIp);
    }

    private OnlinePaymentResponse buildPaymentUrl(UUID propertyId, UUID invoiceId,
                                                  OnlinePaymentRequest request, String clientIp) {
        if (!props.isConfigured()) {
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_NOT_CONFIGURED);
        }

        Invoice invoice = invoiceRepository.findByIdAndPropertyIdAndDeletedFalse(invoiceId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        if (invoice.getStatus() != InvoiceStatus.ISSUED && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID) {
            throw new AppException(ErrorCode.INVOICE_NOT_PAYABLE);
        }

        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        BigDecimal amount = (request != null && request.getAmount() != null) ? request.getAmount() : remaining;
        // VNPay chỉ nhận số nguyên đồng
        if (amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 0) {
            throw new AppException(ErrorCode.PAYMENT_AMOUNT_INVALID);
        }
        if (amount.compareTo(remaining) > 0) {
            throw new AppException(ErrorCode.PAYMENT_EXCEEDS_REMAINING);
        }

        String txnRef = UUID.randomUUID().toString().replace("-", "");
        PaymentTransaction tx = new PaymentTransaction();
        tx.setInvoice(invoice);
        tx.setProvider(PaymentProvider.VNPAY);
        tx.setTxnRef(txnRef);
        tx.setAmount(amount);
        tx.setStatus(PaymentTransactionStatus.PENDING);
        transactionRepository.save(tx);

        LocalDateTime now = LocalDateTime.now(AppConstant.BUSINESS_ZONE);
        LocalDateTime expire = now.plusMinutes(props.expireMinutes());

        Map<String, String> fields = new TreeMap<>();
        fields.put("vnp_Version", "2.1.0");
        fields.put("vnp_Command", "pay");
        fields.put("vnp_TmnCode", props.tmnCode());
        fields.put("vnp_Amount", amount.movePointRight(2).toBigInteger().toString()); // VNPay nhân 100
        fields.put("vnp_CurrCode", "VND");
        fields.put("vnp_TxnRef", txnRef);
        fields.put("vnp_OrderInfo", "Thanh toan hoa don phong "
                + invoice.getRoom().getCode().replaceAll("[^A-Za-z0-9 ]", ""));
        fields.put("vnp_OrderType", "other");
        fields.put("vnp_Locale", "vn");
        fields.put("vnp_ReturnUrl", props.returnUrl());
        fields.put("vnp_IpAddr", clientIp);
        fields.put("vnp_CreateDate", now.format(VNP_DATE));
        fields.put("vnp_ExpireDate", expire.format(VNP_DATE));

        String url = props.payUrl() + "?" + VnPayUtils.query(fields)
                + "&vnp_SecureHash=" + VnPayUtils.sign(props.hashSecret(), fields);

        return OnlinePaymentResponse.builder()
                .txnRef(txnRef)
                .paymentUrl(url)
                .amount(amount)
                .expiresAt(Instant.now().plusSeconds(props.expireMinutes() * 60))
                .build();
    }


    public Map<String, String> handleIpn(Map<String, String> params) {
        String received = params.get("vnp_SecureHash");
        Map<String, String> fields = signedFields(params);

        if (!hasValidSignature(fields, received)) {
            return response("97", "Invalid signature");
        }

        PaymentTransaction tx = transactionRepository
                .findForUpdate(PaymentProvider.VNPAY, fields.getOrDefault("vnp_TxnRef", "")).orElse(null);
        if (tx == null) {
            return response("01", "Order not found");
        }

        BigDecimal vnpAmount;
        try {
            vnpAmount = new BigDecimal(fields.getOrDefault("vnp_Amount", ""));
        } catch (NumberFormatException e) {
            return response("04", "Invalid amount");
        }
        if (tx.getAmount().movePointRight(2).compareTo(vnpAmount) != 0) {
            return response("04", "Invalid amount");
        }

        if (tx.getStatus() != PaymentTransactionStatus.PENDING) {
            return response("02", "Order already confirmed");
        }

        boolean success = "00".equals(fields.get("vnp_ResponseCode"))
                && "00".equals(fields.get("vnp_TransactionStatus"));
        tx.setProviderTxnNo(fields.get("vnp_TransactionNo"));
        tx.setRawIpn(VnPayUtils.query(fields));

        if (success) {
            Invoice invoice = tx.getInvoice();
            var applied = paymentService.tryApplyPayment(invoice.getProperty().getId(), invoice.getId(),
                    tx.getAmount(), PaymentMethod.VNPAY, "VNPay GD " + tx.getProviderTxnNo());
            tx.setStatus(PaymentTransactionStatus.SUCCESS);
            if (applied.isEmpty()) {
                log.warn("VNPay txn {} thanh toán thành công nhưng không ghi nhận được vào hóa đơn {}: cần đối soát/hoàn tiền",
                        tx.getTxnRef(), invoice.getId());
            }
        } else {
            tx.setStatus(PaymentTransactionStatus.FAILED);
        }
        transactionRepository.save(tx);

        return response("00", "Confirm Success");
    }


    public record ReturnResult(boolean validSignature, boolean success, String responseCode,
                               String txnRef, BigDecimal amount) {}


    @Transactional(readOnly = true)
    public ReturnResult verifyReturn(Map<String, String> params) {
        Map<String, String> fields = signedFields(params);
        if (!hasValidSignature(fields, params.get("vnp_SecureHash"))) {
            return new ReturnResult(false, false, null, null, null);
        }
        BigDecimal amount = null;
        try {
            amount = new BigDecimal(fields.getOrDefault("vnp_Amount", "")).movePointLeft(2);
        } catch (NumberFormatException ignored) {

        }
        boolean success = "00".equals(fields.get("vnp_ResponseCode"))
                && "00".equals(fields.get("vnp_TransactionStatus"));
        return new ReturnResult(true, success, fields.get("vnp_ResponseCode"), fields.get("vnp_TxnRef"), amount);
    }

    private static Map<String, String> signedFields(Map<String, String> params) {
        Map<String, String> fields = new TreeMap<>();
        params.forEach((k, v) -> {
            if (k.startsWith("vnp_") && !k.equals("vnp_SecureHash") && !k.equals("vnp_SecureHashType")) {
                fields.put(k, v);
            }
        });
        return fields;
    }

    private boolean hasValidSignature(Map<String, String> fields, String received) {
        return props.isConfigured() && received != null && VnPayUtils.verify(props.hashSecret(), fields, received);
    }

    public static Map<String, String> response(String code, String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("RspCode", code);
        body.put("Message", message);
        return body;
    }
}