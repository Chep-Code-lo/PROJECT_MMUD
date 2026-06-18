# Giai doan 7: Giai thich chi tiet code va y tuong

Stage 7 tren de bai la `Role Authorization`, nhung trong repo thuc te phan auth cu cua Stage 4 van con dang stub. Vi vay luc lam tiep, phan implementation da phai hoan thien auth/JWT that roi moi khoa role duoc. File nay giai thich ro quyet dinh do.

## 1. Muc tieu cua Stage 7

- Chot 3 role: `ADMIN`, `STAFF`, `USER`
- Phan biet ro `401` va `403`
- Khong cho `USER` cham vao route quan tri
- Van de Swagger mo de demo va test

## 2. Cac file chinh da lam

- `backend/src/main/java/com/company/securityapp/config/SecurityConfig.java`
- `backend/src/main/java/com/company/securityapp/entity/User.java`
- `backend/src/main/java/com/company/securityapp/entity/Role.java`
- `backend/src/main/java/com/company/securityapp/repository/UserRepository.java`
- `backend/src/main/java/com/company/securityapp/security/CustomUserDetailsService.java`
- `backend/src/main/java/com/company/securityapp/security/JwtService.java`
- `backend/src/main/java/com/company/securityapp/security/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/company/securityapp/service/AuthService.java`
- `backend/src/main/java/com/company/securityapp/controller/AuthController.java`
- `backend/src/main/java/com/company/securityapp/config/DemoUserInitializer.java`
- `backend/src/test/java/com/company/securityapp/AuthSecurityIntegrationTest.java`

## 3. Role matrix cuoi cung

| Route | Quyen |
|---|---|
| `/api/auth/register`, `/api/auth/login` | Public |
| `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`, `/api/health` | Public |
| `/api/auth/me` | Chi can da xac thuc |
| `/api/admin/**` | `ADMIN` |
| `/api/audit-logs/**` | `ADMIN` |
| `/api/customers/**` | `ADMIN`, `STAFF` |
| `/api/tickets/**` | `ADMIN`, `STAFF`, `USER` |

Stage 7 la noi khoa matrix nay lai trong `SecurityConfig`.

## 4. Vi sao phai hoan thien auth that truoc khi khoa role

Neu auth chi la stub thi role authorization khong co gia tri that, vi:

- khong co token that de validate
- khong co principal that trong security context
- khong co user/role that trong database

Nen implementation da di theo thu tu hop ly:

1. Tao `User` entity that.
2. Hash password bang bcrypt.
3. Tao `register/login/me`.
4. Phat JWT.
5. Dua user vao `SecurityContext`.
6. Sau do moi ap role rule.

No khong phai lan sang scope vo ly, ma la prerequisite ky thuat de Stage 7 chay dung.

## 5. `User` entity duoc thiet ke the nao

`User` implement `UserDetails`.

Y nghia:

- entity database co the di thang vao Spring Security
- `getAuthorities()` tra ve `ROLE_ADMIN`, `ROLE_STAFF`, `ROLE_USER`
- khong can tao them adapter class dai dong

`passwordHash` duoc dat `@JsonIgnore` de tranh lo hash ra response.

## 6. JWT flow hoat dong ra sao

### `AuthService`

- `register`: tao user moi, role mac dinh `USER`, hash password bang bcrypt
- `login`: authenticate, tao JWT, tra `AuthResponse`
- `me`: doc principal hien tai de tra ve user dang dang nhap

### `JwtService`

- tao token
- doc subject tu token
- kiem tra token con han va dung user

### `JwtAuthenticationFilter`

Moi request protected se:

1. Doc header `Authorization`
2. Neu co prefix `Bearer `
3. Tach JWT
4. Extract username
5. Load user tu database
6. Validate token
7. Gan `Authentication` vao `SecurityContext`

Neu token sai, filter clear context va request sau do se bi `401`.

## 7. `401` va `403` khac nhau o dau

`SecurityConfig` co custom:

- `AuthenticationEntryPoint`
- `AccessDeniedHandler`

Nen he thong tra JSON sach, khong phai HTML loi mac dinh.

### `401 Unauthorized`

Tra khi:

- khong co token
- token sai
- token het han

Message:

```text
Authentication is required or the token is invalid.
```

### `403 Forbidden`

Tra khi:

- da dang nhap thanh cong
- nhung role khong du quyen goi route

Message:

```text
You do not have permission to access this resource.
```

Day la diem demo rat quan trong vi de bai yeu cau phan biet ro 2 loai loi nay.

## 8. Demo user duoc seed nhu the nao

`DemoUserInitializer` tao san 3 tai khoan:

- `admin@securityapp.local`
- `staff@securityapp.local`
- `user@securityapp.local`

Password mac dinh:

```text
Password@123
```

Cach lam nay giup:

- Postman demo nhanh
- frontend login demo nhanh
- khong can chen bcrypt hash co dinh vao `seed.sql`

## 9. Test va bang chung

`AuthSecurityIntegrationTest` da cover:

- register
- login
- me
- token thieu/sai => `401`
- `USER` goi `/api/customers` => `403`
- `USER` goi `/api/admin/summary` => `403`
- `ADMIN` goi `/api/admin/summary` => `200`

Ngoai test, stack Docker cung da duoc smoke test ngay `2026-06-18`:

- login bang `admin@securityapp.local`
- goi `/api/auth/me`
- goi `/api/admin/summary`
- login bang `user@securityapp.local`
- goi `/api/customers` nhan `403`

Stage 7 vi vay da chot xong phan authorization dung nghia, khong chi la doi role tren giao dien.
