# Trọ Hub - Mobile App (Android)

Ứng dụng di động quản lý nhà trọ hiện đại, tinh gọn và tối ưu trải nghiệm người dùng (UX) dành cho chủ nhà trọ và quản lý khu trọ. Ứng dụng được xây dựng hoàn toàn bằng **Kotlin**, **Jetpack Compose** (Material Design 3) và kiến trúc **MVVM (Model - View - ViewModel)**.

---

## 🚀 Công nghệ & Kiến trúc

| Thành phần | Công nghệ / Thư viện | Mục đích sử dụng |
|---|---|---|
| **Ngôn ngữ** | Kotlin 2.0+ | Ngôn ngữ chính, an toàn null-safety và coroutines |
| **Giao diện (UI)** | Jetpack Compose + Material Design 3 (M3) | Declarative UI hiện đại, mượt mà, hỗ trợ giao diện đáp ứng |
| **Kiến trúc** | MVVM + StateFlow + Coroutines | Tách biệt giao diện và logic, Single Source of Truth |
| **Mạng (Network)** | Retrofit 2 + OkHttp 3 + Gson | Kết nối RESTful API, tự động gắn Bearer Token và xử lý fallback IP |
| **Sinh trắc học** | `androidx.biometric:biometric` | Đăng nhập bằng Vân tay / Nhận diện khuôn mặt an toàn |
| **Paging & UX** | `androidx.compose.foundation.pager.HorizontalPager` | Màn hình Onboarding 3 trang mượt mà không dùng thư viện ngoài |
| **Hiệu ứng Shimmer** | Pure Compose Infinite Transition | Hiệu ứng chuyển động Skeleton ánh kim khi tải dữ liệu |
| **Lưu trữ cục bộ** | SharedPreferences / TokenManager | Lưu JWT Token, Khu trọ đang chọn, Cấu hình ngân hàng VietQR |

---

## 🌟 Các tính năng nổi bật đã hoàn thành

### 1. Màn hình Giới thiệu (Onboarding Screens)
- Sử dụng `HorizontalPager` native của Jetpack Compose Foundation.
- Gồm 3 trang giới thiệu nghiệp vụ cốt lõi: Quản lý phòng & Khách thuê, Ghi chỉ số Điện Nước, Hóa đơn & Mã VietQR tự động.
- Thanh chỉ báo vị trí (Page Indicator) dạng viên nhộng động (dynamic capsule indicator) chuyển kích thước mượt mà.
- Lưu cờ trạng thái vào bộ nhớ máy, tự động bỏ qua ở các lần mở app tiếp theo.

### 2. Xác thực & Bảo mật (Auth & Security)
- Đăng ký tài khoản chủ trọ mới và Đăng nhập bảo mật JWT.
- **Đăng nhập Sinh trắc học (Biometric Authentication)**: Quét vân tay hoặc khuôn mặt bằng `BiometricPrompt` chính thức, tự động fallback nếu máy không có cảm biến.
- **Cấu hình máy chủ linh hoạt (Dynamic Server Config)**: Cho phép chuyển đổi nhanh địa chỉ API giữa máy ảo Android Emulator (`10.0.2.2:8080`) và thiết bị thật qua mạng LAN (`192.168.x.x:8080`).

### 3. Bảng điều khiển Trang chủ (Home Dashboard)
- Chuyển đổi và quản lý đồng thời nhiều khu trọ (`PropertySelectorHeader`).
- **Thẻ Quản lý thu trọ tháng này**: Hiển thị tổng doanh thu dự kiến thu chính xác, bao gồm:
  - Tiền phòng thuê thực tế.
  - Tiền điện, nước tạm tính và các phí dịch vụ đã ghi.
- Thống kê tỷ lệ lấp đầy phòng, số lượng phòng trống sẵn sàng đón khách, tiền cọc đang giữ.
- 4 phím tắt tiện ích mở nhanh các nghiệp vụ quản lý.

### 4. Quản lý Danh sách Phòng (Room Management)
- Xem danh sách phòng theo dạng thẻ trực quan (số phòng, giá thuê, diện tích, tầng, khách đang thuê).
- Bộ lọc đa năng theo trạng thái (`Tất cả`, `Đang thuê`, `Trống`, `Bảo trì`) và thanh tìm kiếm thời gian thực.
- Thêm mới phòng trọ theo khu, tầng và diện tích.
- **Nút "Bảo trì / Đưa vào sử dụng" phản hồi tức thì**: Áp dụng kỹ thuật **Optimistic UI Update** và hiệu ứng xoay tròn `CircularProgressIndicator`, không bị đơ giật.
- Hỗ trợ tạo hợp đồng cho thuê phòng nhanh từ khách đã có hoặc thêm khách mới ngay tại chỗ.

### 5. Quản lý Khách thuê & Liên hệ nhanh (Tenants & Contact Intents)
- Quản lý danh bạ khách thuê: Họ tên, số điện thoại, CCCD/CMND, ngày sinh, ghi chú phòng.
- Thêm mới, cập nhật thông tin và xóa hồ sơ người thuê.
- **Thẻ Liên hệ nhanh (ContactCard)**:
  - **Gọi điện (`Intent.ACTION_DIAL`)**: Mở bàn phím gọi điện thoại an toàn, **không cần xin quyền nguy hiểm `CALL_PHONE`**, bảo đảm chính sách Google Play Store.
  - **Nhắn Zalo (`Intent.ACTION_VIEW`)**: Mở ứng dụng Zalo trực tiếp qua liên kết `https://zalo.me/{phone}`, có xử lý ngoại lệ và thông báo Toast nếu máy chưa cài Zalo.

### 6. Ghi chỉ số Điện & Nước (Meter Readings)
- Bảng nhập chỉ số công tơ điện và đồng hồ nước từng phòng theo kỳ.
- Tự động tính toán lượng điện tiêu thụ (kWh) và lượng nước (m³).
- Cảnh báo trực quan nếu chỉ số mới nhỏ hơn chỉ số cũ để phòng tránh sai sót.

### 7. Hóa đơn & Thanh toán VietQR (Billing & VietQR)
- Tự động tổng hợp hóa đơn chi tiết từng phòng: `Tiền phòng + Tiền điện + Tiền nước + Dịch vụ khác`.
- **Hiệu ứng Skeleton Shimmer**: Áp dụng `Modifier.shimmerEffect()` hiển thị thẻ giả lập chuyển động ánh kim trong lúc tải dữ liệu, thay thế vòng xoay truyền thống.
- **Tích hợp mã VietQR chuẩn NAPAS 247**: Tự động tạo mã QR chứa thông tin STK ngân hàng, tên chủ trọ, số tiền chính xác và cú pháp chuyển khoản theo từng số phòng.
- Chuyển trạng thái thanh toán `Chờ thanh toán` ↔ `Đã thu tiền`.

### 8. Báo cáo doanh thu & Cài đặt (Reports & Settings)
- Báo cáo tổng quan tình hình tài chính theo tháng và năm.
- Cài đặt cấu hình tài khoản ngân hàng thụ hưởng nhận tiền chuyển khoản.
- Đăng xuất an toàn, xóa phiên làm việc.

---

## 📂 Cấu trúc thư mục dự án Mobile

```
mobile/app/src/main/java/com/uth/trohub/
├── MainActivity.kt                # Single-Activity kế thừa FragmentActivity hỗ trợ Biometric
├── data/
│   ├── local/                     # TokenManager lưu trữ SharedPreferences
│   ├── model/                     # Data classes Request/Response khớp với Backend
│   └── network/                   # Retrofit Client, Auth Interceptor, ApiServices
├── navigation/
│   ├── AppRoute.kt                # Định nghĩa các Route điều hướng
│   └── BottomNavItem.kt           # Định nghĩa 5 Tabs thanh điều hướng dưới đáy
├── ui/
│   ├── components/                # Các thành phần giao diện dùng chung
│   │   ├── AppBottomBar.kt        # Thanh điều hướng Bottom Navigation Bar
│   │   ├── AppTopBar.kt           # Thanh tiêu đề phía trên
│   │   ├── ContactCard.kt         # Thẻ Gọi điện & Nhắn tin Zalo nhanh
│   │   ├── ShimmerModifier.kt     # Modifier hiệu ứng Skeleton Shimmer
│   │   ├── SkeletonInvoiceCard.kt # Thẻ Hóa đơn giả lập Skeleton Loading
│   │   └── StatusChip.kt          # Chip trạng thái (Đang thuê, Trống, Bảo trì)
│   ├── screens/                   # Các màn hình chức năng và ViewModel tương ứng
│   │   ├── auth/                  # Màn hình Đăng nhập & Đăng ký
│   │   ├── home/                  # Màn hình Trang chủ Dashboard
│   │   ├── invoices/              # Màn hình Danh sách & Chi tiết Hóa đơn, VietQR
│   │   ├── meters/                # Màn hình Ghi chỉ số Điện & Nước
│   │   ├── onboarding/            # Màn hình Giới thiệu HorizontalPager
│   │   ├── reports/               # Màn hình Báo cáo doanh thu
│   │   ├── rooms/                 # Màn hình Quản lý danh sách Phòng trọ
│   │   ├── settings/              # Màn hình Cài đặt & Cấu hình
│   │   └── tenants/               # Màn hình Quản lý khách thuê trọ
│   └── theme/                     # Định nghĩa màu sắc Mint/Green, Kiểu chữ và Theme M3
└── util/
    └── BiometricAuthHelper.kt     # Helper xác thực vân tay / khuôn mặt BiometricPrompt
```

---

## 🛠 Hướng dẫn Cài đặt & Khởi chạy

### Yêu cầu tiên quyết:
- **Android Studio**: Koala / Ladybug (2024.1+) hoặc mới hơn.
- **JDK**: Java 17 hoặc Java 21.
- **Android SDK**: `compileSdk = 37`, `minSdk = 24`, `targetSdk = 37`.

### Các bước khởi chạy:
1. Mở thư mục `mobile` bằng Android Studio.
2. Đồng bộ Gradle (Sync Project with Gradle Files).
3. Khởi động máy ảo Android Emulator hoặc kết nối thiết bị thật (bật USB Debugging).
4. Chạy lệnh cài đặt debug qua terminal:
   ```bash
   cd mobile
   ./gradlew installDebug
   ```
5. Đảm bảo Backend Spring Boot đang chạy ở cổng `8080`.
   - Đối với **Android Emulator**: Địa chỉ API mặc định là `http://10.0.2.2:8080/api/v1/`.
   - Đối với **Thiết bị thật (cùng Wi-Fi)**: Địa chỉ API là `http://<IP_MÁY_TÍNH>:8080/api/v1/` (có thể đổi trực tiếp ngay tại biểu tượng ⚙ dưới màn hình đăng nhập).
