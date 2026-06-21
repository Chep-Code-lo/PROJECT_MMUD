# Giai đoạn 9: Giải thích chi tiết code và ý tưởng

Giai đoạn 9 là bước nối frontend thật vào backend thật. Mục tiêu là biến giao diện từ phần minh họa thành một lớp trình diễn đầy đủ cho `authentication`, `authorization`, `customer flow` và `audit flow`.

## 1. Mục tiêu của giai đoạn 9

- frontend không dùng mock data nữa
- login và register gọi backend thật
- có `ProtectedRoute`
- menu thay đổi theo role
- xử lý `401` và `403`
- demo được customer flow và audit flow bằng giao diện

## 2. Các file chính của giai đoạn

- `frontend/lib/axiosClient.ts`
- `frontend/lib/tokenStorage.ts`
- `frontend/services/authService.ts`
- `frontend/services/customerService.ts`
- `frontend/services/auditLogService.ts`
- `frontend/components/ProtectedRoute.tsx`
- `frontend/components/Navbar.tsx`
- `frontend/components/CustomerTable.tsx`
- `frontend/components/AuditLogTable.tsx`
- `frontend/app/login/page.tsx`
- `frontend/app/register/page.tsx`
- `frontend/app/dashboard/page.tsx`
- `frontend/app/customers/page.tsx`
- `frontend/app/customers/new/page.tsx`
- `frontend/app/audit-logs/page.tsx`

## 3. Kiến trúc frontend được tách như thế nào

### `axiosClient`

Đây là lớp giao tiếp HTTP dùng chung.

Hiện tại `axiosClient` làm 3 việc chính:

- dùng `NEXT_PUBLIC_API_URL` nếu được cấu hình
- nếu không có biến môi trường thì dùng same-origin, tức là frontend gọi trực tiếp `/api/...` qua `Nginx`
- tự chèn `Authorization: Bearer <token>` nếu token tồn tại

Điểm này quan trọng vì runtime hiện tại không còn mặc định bắn thẳng vào `http://localhost:8080` nữa.

### `tokenStorage`

Tách riêng logic lưu token trong `localStorage` để:

- dễ thay đổi cách lưu sau này
- `authService` và `axiosClient` dùng chung một nơi

### `services/*`

Mỗi nghiệp vụ có một service:

- `authService`
- `customerService`
- `auditLogService`

UI page không gọi `axios` trực tiếp, mà đi qua service để code rõ hơn và dễ kiểm soát hơn.

## 4. `ProtectedRoute` đang làm gì

`ProtectedRoute` là lớp khóa ở cấp trang.

Luồng hoạt động:

1. kiểm tra token có tồn tại không
2. gọi `/api/auth/me` để lấy user hiện tại
3. so role hiện tại với `allowedRoles`
4. nếu không hợp lệ thì chuyển về login hoặc chặn truy cập

Ý nghĩa:

- tránh lộ route ở tầng UI
- đồng bộ logic role giữa frontend và backend
- giúp demo trực quan hơn khi bị chặn quyền

## 5. `Navbar` và `Dashboard` đã đổi vai trò như thế nào

`Navbar` không còn hiển thị cùng một menu cho mọi role.

- `ADMIN`, `STAFF` nhìn thấy `Customers`
- `ADMIN` nhìn thấy thêm `Audit Logs`
- user đã đăng nhập nhìn thấy session hiện tại và nút logout

`Dashboard` được dùng như màn hình tổng quan:

- hiển thị user hiện tại
- nhắc lại scope bảo mật của dự án
- điều hướng nhanh đến `Customers` và `Audit Logs` tùy role

## 6. Customer flow trên UI

Luồng chính:

1. đăng nhập
2. vào `/customers`
3. gọi `GET /api/customers`
4. vào form tạo mới
5. gọi `POST /api/customers`
6. sau khi thành công quay lại danh sách

Điểm quan trọng:

- frontend chỉ nhìn thấy dữ liệu đã được backend giải mã
- frontend không biết và không thấy các cột `_encrypted`
- nếu token hết hạn hoặc không hợp lệ thì `axiosClient` xóa token và đẩy người dùng về `/login`

## 7. Audit flow trên UI

Chỉ `ADMIN` mới nhìn thấy route:

- `/audit-logs`

Page này gọi:

- `GET /api/audit-logs`

Mục tiêu của route này là để khi demo, giảng viên có thể nhìn trực tiếp trên UI các sự kiện như:

- login success
- login failed
- create customer
- update customer
- delete customer

## 8. Liên hệ với local và public domain

Frontend hiện chạy được trên cả hai chế độ:

- local: `https://localhost`
- public domain: `https://demo.hackerlo.online`

Điểm quan trọng là hành vi frontend không đổi giữa hai mode:

- cùng giao diện
- cùng flow login
- cùng protected route
- cùng gọi API qua origin hiện tại

`demo.hackerlo.online` chỉ hoạt động khi `Cloudflare Tunnel` đang bật. Nếu public mode không có, cả nhóm vẫn có thể chạy và test đầy đủ qua `https://localhost`.

## 9. Vì sao giai đoạn 9 quan trọng

Nếu chỉ có backend thì đề tài vẫn đúng, nhưng lúc demo sẽ khó thể hiện luồng bảo mật.

Frontend thật giúp:

- nhìn thấy rõ login và logout
- nhìn thấy rõ menu đổi theo role
- nhìn thấy rõ `401` và `403` theo hành vi người dùng
- chứng minh API contract đã đủ ổn định để dùng thật

Nói cách khác, giai đoạn 9 biến các security control thành trải nghiệm có thể thuyết trình.

## 10. Cách tóm tắt khi thuyết trình

> Giai đoạn 9 của em là bỏ mock data và nối frontend vào backend thật. Em dùng `axiosClient` để tự gắn JWT, `ProtectedRoute` để khóa trang theo role, và giữ giao diện tập trung đúng các flow cần demo: đăng nhập, customer và audit log. Vì vậy các control bảo mật không chỉ nằm ở backend mà còn nhìn thấy được rõ trên UI.
