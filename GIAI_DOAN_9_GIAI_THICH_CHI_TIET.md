# Giai doan 9: Giai thich chi tiet code va y tuong

Stage 9 la buoc bien frontend tu scaffold thanh giao dien co the demo auth, authorization, customer, ticket va audit bang API that.

## 1. Muc tieu cua Stage 9

- Frontend khong dung mock data nua
- Login/Register goi backend that
- Co protected route
- Menu an/hien theo role
- Xu ly `401` va `403`
- Demo duoc customer flow, ticket flow va audit flow

## 2. Cac file chinh da lam

- `frontend/lib/axiosClient.ts`
- `frontend/lib/tokenStorage.ts`
- `frontend/services/authService.ts`
- `frontend/services/customerService.ts`
- `frontend/services/ticketService.ts`
- `frontend/services/auditLogService.ts`
- `frontend/components/ProtectedRoute.tsx`
- `frontend/components/Navbar.tsx`
- `frontend/components/CustomerTable.tsx`
- `frontend/components/TicketTable.tsx`
- `frontend/components/AuditLogTable.tsx`
- `frontend/app/login/page.tsx`
- `frontend/app/register/page.tsx`
- `frontend/app/dashboard/page.tsx`
- `frontend/app/customers/page.tsx`
- `frontend/app/customers/new/page.tsx`
- `frontend/app/tickets/page.tsx`
- `frontend/app/tickets/new/page.tsx`
- `frontend/app/audit-logs/page.tsx`

## 3. Kien truc frontend duoc tach the nao

### `axiosClient`

Day la lop giao tiep HTTP dung chung.

No lam 3 viec:

- set `baseURL`
- chen `Authorization: Bearer ...` neu co token
- bat `401` de xoa token va day nguoi dung ve trang login

Mac dinh `NEXT_PUBLIC_API_URL` neu co, neu khong thi fallback ve:

```text
http://localhost:8080
```

Quyet dinh nay quan trong vi giup frontend van demo duoc ngay ca khi env chua khai bao day du.

### `tokenStorage`

Tach rieng logic luu token vao local storage de:

- de doi cach luu sau nay
- service auth va axios dung chung 1 noi

### `services/*`

Moi nghiep vu co 1 service:

- `authService`
- `customerService`
- `ticketService`
- `auditLogService`

UI page khong goi `axios` truc tiep nua. No chi goi service. Kieu tach nay lam code de doc hon va de doi contract hon.

## 4. `ProtectedRoute` dang lam gi

`ProtectedRoute` la khoa bao ve o cap page.

Moi page protected se:

1. Kiem tra token co ton tai khong
2. Goi `authService.getCurrentUser()`
3. Neu khong hop le thi day ve `/login`
4. Neu role khong dung thi hien man hinh `403 Forbidden`

Day la cach don gian nhung hieu qua de demo authorization ngay tren giao dien.

## 5. Navbar thay doi theo role

`Navbar` load current user va chi hien menu hop le:

- `Customers`: chi `ADMIN` va `STAFF`
- `Tickets`: tat ca role da dang nhap
- `Audit Logs`: chi `ADMIN`

No giup giao dien khong moi nguoi dung bam vao nhung chuc nang ho khong du quyen.

Nhung quan trong hon, day chi la lop UI. Backend van la noi khoa chot cuoi cung. Neu ai do co tinh goi API truc tiep sai role thi backend van tra `403`.

## 6. Login, register va dashboard da doi the nao

### Login

- Goi `POST /api/auth/login`
- Lay `accessToken`
- Luu vao `tokenStorage`
- Chuyen vao dashboard
- Hien san 3 demo account de test nhanh

### Register

- Goi `POST /api/auth/register`
- Dang ky that voi backend
- Note ro tai khoan moi mac dinh co role `USER`

### Dashboard

- Goi `GET /api/auth/me`
- Hien thong tin session hien tai
- Hien card khac nhau theo role

Dashboard o day khong co muc tieu dep mat. No co muc tieu demo ro rang xem minh dang dang nhap voi role nao.

## 7. Customer flow va Ticket flow tren UI

### Customer flow

- Page customers list goi `GET /api/customers`
- Form tao customer goi `POST /api/customers`
- Table co delete de goi `DELETE /api/customers/{id}`

### Ticket flow

- Page tickets list goi `GET /api/tickets`
- Form tao ticket goi `POST /api/tickets`
- Form tao ticket tu dong lay `currentUser.id` de gui vao `createdById`
- Table co nut doi status bang `PATCH /api/tickets/{id}/status`
- Table co nut delete ticket

Luu y quan trong:

- Ticket flow dung `customerId` dung theo contract backend moi
- Status frontend da doi sang enum that: `OPEN`, `PROCESSING`, `RESOLVED`

## 8. Man hinh Audit Log

Stage 9 da noi luon frontend vao Stage 8 bang:

- `frontend/app/audit-logs/page.tsx`
- `frontend/components/AuditLogTable.tsx`
- `frontend/services/auditLogService.ts`

Man hinh nay chi cho `ADMIN`.

Y nghia demo:

- login
- tao customer
- tao ticket
- doi status
- vao audit logs

Nguoi cham bai se thay ro chuoi hanh dong bao mat ngay tren UI.

## 9. Xu ly `401` va `403`

### `401`

`axiosClient` interceptor bat `401` va:

- xoa token
- redirect ve `/login`

### `403`

`ProtectedRoute` se hien page `403 Forbidden` neu token hop le nhung role sai.

Su tach biet nay giup giao dien khop voi logic Stage 7 o backend.

## 10. Test va bang chung

Frontend da build pass:

```bash
cd frontend
npm run build
```

Ngoai ra, stack Docker hien tai da chay duoc `frontend` o `http://localhost:3000` va backend that o `http://localhost:8080`.

No chung minh Stage 9 da noi frontend vao API that, khong con dung bo mock data cu.
