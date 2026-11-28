package me.ihqqq.trohub.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_KEY(1001, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1002, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1003, "Chưa đăng nhập hoặc phiên đăng nhập không hợp lệ", HttpStatus.UNAUTHORIZED),
    TOO_MANY_LOGIN_ATTEMPTS(1005, "Đăng nhập sai quá nhiều lần, vui lòng thử lại sau 15 phút", HttpStatus.TOO_MANY_REQUESTS),
    UNAUTHORIZED(1004, "Bạn không có quyền thực hiện thao tác này", HttpStatus.FORBIDDEN),

    USER_EXISTED(1101, "Tên đăng nhập đã tồn tại", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1102, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(1103, "Tên đăng nhập phải từ 4 đến 50 ký tự", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1104, "Mật khẩu phải từ 8 đến 72 ký tự", HttpStatus.BAD_REQUEST),
    FULLNAME_REQUIRED(1105, "Họ tên không được để trống", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1106, "Email không hợp lệ", HttpStatus.BAD_REQUEST),
    CREDENTIAL_REQUIRED(1107, "Tên đăng nhập và mật khẩu không được để trống", HttpStatus.BAD_REQUEST),

    PROPERTY_NOT_FOUND(1201, "Không tìm thấy nhà trọ", HttpStatus.NOT_FOUND),
    PROPERTY_NAME_REQUIRED(1202, "Tên nhà trọ không được để trống", HttpStatus.BAD_REQUEST),
    MEMBER_EXISTED(1203, "Người dùng đã thuộc nhà trọ này", HttpStatus.BAD_REQUEST),
    MEMBER_NOT_FOUND(1204, "Không tìm thấy thành viên trong nhà trọ", HttpStatus.NOT_FOUND),
    CANNOT_REMOVE_OWNER(1205, "Không thể xóa hoặc đổi vai trò chủ trọ", HttpStatus.BAD_REQUEST),
    MEMBER_ROLE_INVALID(1206, "Chỉ có thể thêm thành viên với vai trò quản lý", HttpStatus.BAD_REQUEST),
    USERNAME_REQUIRED(1207, "Tên đăng nhập không được để trống", HttpStatus.BAD_REQUEST),
    ROLE_REQUIRED(1208, "Vai trò không được để trống", HttpStatus.BAD_REQUEST),
    BANK_INFO_INCOMPLETE(1209, "Phải nhập đủ tên ngân hàng, số tài khoản và tên chủ tài khoản", HttpStatus.BAD_REQUEST),
    PROPERTY_HAS_ACTIVE_DATA(1211, "Nhà trọ còn hợp đồng đang hiệu lực hoặc hóa đơn chưa thanh toán, không thể xóa", HttpStatus.BAD_REQUEST),
    BANK_ACCOUNT_INVALID(1210, "Số tài khoản chỉ gồm chữ số, dài 6 đến 19 ký tự", HttpStatus.BAD_REQUEST),

    ROOM_NOT_FOUND(1301, "Không tìm thấy phòng", HttpStatus.NOT_FOUND),
    ROOM_CODE_REQUIRED(1302, "Số phòng không được để trống", HttpStatus.BAD_REQUEST),
    ROOM_CODE_EXISTED(1303, "Số phòng đã tồn tại trong nhà trọ", HttpStatus.BAD_REQUEST),
    ROOM_PRICE_INVALID(1304, "Giá phòng phải lớn hơn hoặc bằng 0", HttpStatus.BAD_REQUEST),
    ROOM_STATUS_INVALID(1305, "Không thể đổi trạng thái phòng theo cách này", HttpStatus.BAD_REQUEST),
    ROOM_NOT_AVAILABLE(1306, "Phòng không ở trạng thái trống", HttpStatus.BAD_REQUEST),
    ROOM_HAS_ACTIVE_CONTRACT(1307, "Phòng đang có hợp đồng hiệu lực", HttpStatus.BAD_REQUEST),
    STATUS_REQUIRED(1308, "Trạng thái không được để trống", HttpStatus.BAD_REQUEST),

    TENANT_NOT_FOUND(1401, "Không tìm thấy người thuê", HttpStatus.NOT_FOUND),
    TENANT_NAME_REQUIRED(1402, "Họ tên người thuê không được để trống", HttpStatus.BAD_REQUEST),
    TENANT_HAS_ACTIVE_CONTRACT(1403, "Người thuê đang có hợp đồng hiệu lực", HttpStatus.BAD_REQUEST),

    CONTRACT_NOT_FOUND(1501, "Không tìm thấy hợp đồng", HttpStatus.NOT_FOUND),
    CONTRACT_DATE_INVALID(1502, "Ngày kết thúc phải sau ngày bắt đầu", HttpStatus.BAD_REQUEST),
    CONTRACT_NOT_ACTIVE(1503, "Hợp đồng không còn hiệu lực", HttpStatus.BAD_REQUEST),
    CONTRACT_ROOM_REQUIRED(1504, "Phải chọn phòng", HttpStatus.BAD_REQUEST),
    CONTRACT_TENANT_REQUIRED(1505, "Phải chọn người thuê", HttpStatus.BAD_REQUEST),
    CONTRACT_START_REQUIRED(1506, "Ngày bắt đầu không được để trống", HttpStatus.BAD_REQUEST),
    CONTRACT_OCCUPANTS_INVALID(1508, "Số người ở phải từ 1 trở lên", HttpStatus.BAD_REQUEST),
    CONTRACT_RENEW_DATE_INVALID(1509, "Ngày kết thúc mới phải sau hôm nay và sau ngày bắt đầu hợp đồng", HttpStatus.BAD_REQUEST),
    CONTRACT_END_DATE_INVALID(1510, "Ngày kết thúc phải từ ngày bắt đầu hợp đồng đến hôm nay", HttpStatus.BAD_REQUEST),
    CONTRACT_NOT_ENDED(1511, "Chỉ quyết toán tiền cọc cho hợp đồng đã kết thúc", HttpStatus.BAD_REQUEST),
    DEPOSIT_SETTLEMENT_INVALID(1512, "Số hoàn trả và giữ lại phải từ 0 trở lên, tổng không vượt tiền cọc của hợp đồng", HttpStatus.BAD_REQUEST),
    CONTRACT_AMOUNT_INVALID(1507, "Số tiền không được âm", HttpStatus.BAD_REQUEST),

    SERVICE_NOT_FOUND(1601, "Không tìm thấy dịch vụ", HttpStatus.NOT_FOUND),
    SERVICE_NAME_REQUIRED(1602, "Tên dịch vụ không được để trống", HttpStatus.BAD_REQUEST),
    SERVICE_TYPE_REQUIRED(1603, "Loại dịch vụ không được để trống", HttpStatus.BAD_REQUEST),
    SERVICE_UNIT_REQUIRED(1604, "Đơn vị tính không được để trống", HttpStatus.BAD_REQUEST),
    SERVICE_TIER_INVALID(1606, "Bậc giá không hợp lệ: tối đa 10 bậc, cận trên phải tăng dần, bậc cuối để trống cận trên", HttpStatus.BAD_REQUEST),
    SERVICE_TIER_NOT_ALLOWED(1607, "Chỉ dịch vụ điện/nước mới có giá bậc thang", HttpStatus.BAD_REQUEST),
    SERVICE_PRICE_INVALID(1605, "Đơn giá phải lớn hơn hoặc bằng 0", HttpStatus.BAD_REQUEST),

    METER_ROOM_REQUIRED(1701, "Phải chọn phòng để ghi chỉ số", HttpStatus.BAD_REQUEST),
    METER_SERVICE_REQUIRED(1702, "Phải chọn dịch vụ điện/nước", HttpStatus.BAD_REQUEST),
    METER_PERIOD_REQUIRED(1703, "Kỳ ghi chỉ số không được để trống", HttpStatus.BAD_REQUEST),
    METER_CURRENT_REQUIRED(1704, "Chỉ số mới không được để trống", HttpStatus.BAD_REQUEST),
    METER_VALUE_INVALID(1705, "Chỉ số mới phải lớn hơn hoặc bằng chỉ số cũ và không được âm", HttpStatus.BAD_REQUEST),
    METER_SERVICE_TYPE_INVALID(1706, "Dịch vụ này không phải loại tính theo chỉ số (điện/nước)", HttpStatus.BAD_REQUEST),
    METER_READING_EXISTED(1707, "Phòng đã có chỉ số của dịch vụ này trong kỳ", HttpStatus.BAD_REQUEST),

    INVOICE_NOT_FOUND(1801, "Không tìm thấy hóa đơn", HttpStatus.NOT_FOUND),
    INVOICE_PERIOD_REQUIRED(1802, "Kỳ hóa đơn không được để trống", HttpStatus.BAD_REQUEST),
    INVOICE_HAS_PAYMENTS(1803, "Hóa đơn đã có khoản thanh toán, không thể xóa", HttpStatus.BAD_REQUEST),

    PAYMENT_AMOUNT_INVALID(1901, "Số tiền thanh toán phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    PAYMENT_METHOD_REQUIRED(1902, "Phương thức thanh toán không được để trống", HttpStatus.BAD_REQUEST),
    PAYMENT_METHOD_INVALID(1903, "Chỉ ghi nhận thủ công bằng tiền mặt hoặc chuyển khoản", HttpStatus.BAD_REQUEST),
    PAYMENT_EXCEEDS_REMAINING(1904, "Số tiền vượt quá số còn nợ của hóa đơn", HttpStatus.BAD_REQUEST),
    TENANT_ALREADY_LINKED(2102, "Hồ sơ người thuê này đã được liên kết với một tài khoản", HttpStatus.CONFLICT),
    INVITE_CODE_INVALID(2103, "Mã liên kết không đúng hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    TOO_MANY_LINK_ATTEMPTS(2104, "Nhập sai mã quá nhiều lần, vui lòng thử lại sau 15 phút", HttpStatus.TOO_MANY_REQUESTS),
    PAYMENT_AMOUNT_OUT_OF_RANGE(2002, "Số tiền thanh toán online nằm ngoài giới hạn của cổng thanh toán (VNPay: 5.000đ–999.999.999đ, MoMo: 1.000đ–50.000.000đ)", HttpStatus.BAD_REQUEST),
    PAYMENT_GATEWAY_ERROR(2003, "Cổng thanh toán không phản hồi hoặc từ chối yêu cầu", HttpStatus.BAD_GATEWAY),
    PAYMENT_GATEWAY_NOT_CONFIGURED(2001, "Cổng thanh toán online chưa được cấu hình", HttpStatus.SERVICE_UNAVAILABLE),
    INVOICE_HAS_PENDING_PAYMENT(2004, "Hóa đơn đang có giao dịch thanh toán online chờ xử lý, vui lòng thử lại sau ít phút", HttpStatus.CONFLICT),
    TRANSACTION_NOT_FOUND(2005, "Không tìm thấy giao dịch", HttpStatus.NOT_FOUND),
    TRANSACTION_NOT_RECONCILABLE(2006, "Chỉ giao dịch chờ đối soát mới được đánh dấu đã xử lý", HttpStatus.BAD_REQUEST),
    REPORT_RANGE_INVALID(2201, "Khoảng thời gian báo cáo không hợp lệ (tối đa 24 tháng, từ tháng không được sau đến tháng)", HttpStatus.BAD_REQUEST),
    INVOICE_NOT_EDITABLE(1804, "Chỉ sửa được hóa đơn chưa thanh toán đủ", HttpStatus.BAD_REQUEST),
    INVOICE_ITEM_NOT_FOUND(1805, "Không tìm thấy khoản trên hóa đơn", HttpStatus.NOT_FOUND),
    INVOICE_ITEM_NOT_ADJUSTMENT(1806, "Chỉ xóa được khoản phát sinh hoặc giảm giá thêm thủ công", HttpStatus.BAD_REQUEST),
    INVOICE_TOTAL_BELOW_PAID(1807, "Tổng hóa đơn không được thấp hơn số tiền đã thu", HttpStatus.BAD_REQUEST),
    INVOICE_ITEM_DESCRIPTION_REQUIRED(1808, "Mô tả khoản không được để trống (tối đa 200 ký tự)", HttpStatus.BAD_REQUEST),
    INVOICE_ITEM_QUANTITY_INVALID(1809, "Số lượng phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    INVOICE_ITEM_PRICE_INVALID(1810, "Đơn giá không hợp lệ (có thể âm để giảm giá)", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND(1906, "Không tìm thấy khoản thanh toán", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_VOIDABLE(1907, "Chỉ hủy được khoản ghi nhận thủ công (tiền mặt, chuyển khoản)", HttpStatus.BAD_REQUEST),
    INVOICE_NOT_PAYABLE(1905, "Hóa đơn đã thanh toán đủ hoặc không còn hiệu lực", HttpStatus.BAD_REQUEST),
    ;

    int code;
    String message;
    HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}