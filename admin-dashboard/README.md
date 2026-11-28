# TroHub Admin Dashboard

Dashboard React/Vite/TypeScript dành cho OWNER và MANAGER quản lý từng property
được backend cấp quyền.

## Yêu cầu môi trường

Môi trường phát triển đã kiểm chứng:

- Node.js `v24.20.0`
- npm `11.19.0`

## Cài đặt và chạy

Từ thư mục `admin-dashboard/`:

```bash
npm install
npm run dev
```

Mặc định Vite mở development server trên địa chỉ được hiển thị trong terminal.

## Cấu hình API

Tạo `.env.local` từ `.env.example`:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

`.env.local` và các file môi trường thực tế không được commit. Không đặt token,
mật khẩu hoặc secret vào source code hay file được chia sẻ.

Frontend gọi Spring Boot REST API qua API base URL này; không kết nối trực tiếp
đến Supabase hoặc database từ browser.

## Xác thực và phân quyền

- Đăng nhập dùng `POST /auth/token`.
- Profile dùng `GET /users/myInfo`.
- Logout dùng `POST /auth/logout` với contract của backend hiện tại.
- Access token được giữ trong `sessionStorage` cho MVP và gửi qua Bearer
  interceptor tập trung.
- Route `/app` được bảo vệ; `401` sẽ xóa session và đưa người dùng về `/login`.
- `403` được hiển thị như lỗi không có quyền, không tự động đổi role hoặc retry
  vô hạn.
- Property scope được xác định từ `GET /properties` và `PropertyResponse.myRole`.
  Frontend guard chỉ hỗ trợ UX; backend vẫn là nơi quyết định quyền cuối cùng.
- OWNER có thể mutation các resource mà backend cho phép. MANAGER chỉ thấy
  mutation phù hợp với quyền đã xác minh, đặc biệt không có action mutation cho
  service price hoặc property members.

## Tính năng hiện có

- Chọn property theo membership backend.
- Overview dùng dữ liệu thật từ rooms, tenants, active contracts và service
  prices.
- Quản lý phòng: list, filter, tìm kiếm client-side sau khi tải dữ liệu, detail,
  create, update, status và delete.
- Quản lý khách thuê: list, detail, create, update và delete.
- Quản lý hợp đồng: list, filter status, create và terminate.
- Quản lý giá dịch vụ: list; OWNER có create/update/delete.
- Quản lý member theo property: list; OWNER có thể thêm MANAGER hoặc xóa
  MANAGER.
- Có loading, empty, retry, validation và lỗi business/network cho các màn hình
  đã triển khai.

Các request và response được khai báo trong `src/api/types.ts` và gọi qua
`src/api/client.ts`.

## Chưa triển khai vì backend chưa có application API

Dashboard chưa triển khai nghiệp vụ meter reading, invoice, payment,
revenue/report hoặc biểu đồ tài chính. Không dùng mock data để giả lập các phần
này.

Backend hiện cũng không cung cấp endpoint đổi role member, tạo thêm OWNER, sửa
hoặc xóa contract. Dashboard không tạo các thao tác đó.

## Kiểm tra chất lượng

Các lệnh đã chạy trong giai đoạn final verification:

```bash
npm run lint
npm run build
```

Kết quả: lint và production build đều thành công.

Package hiện chưa có script `test`, nên không có automated test command để chạy.
Các luồng runtime với backend thật, tài khoản thật và database runtime chưa được
xác minh trong môi trường này.

## Chưa xác minh được

- Login, logout và các mutation chưa được chạy với backend runtime hoặc tài khoản
  thật trong lượt kiểm tra này.
- Schema/database runtime, Supabase/RLS và dữ liệu production chưa được truy cập
  hoặc xác minh.
- CORS và cấu hình triển khai production chưa được kiểm tra.
