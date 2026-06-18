# Giai doan 7: Giai thich chi tiet code va y tuong

Stage 7 la buoc chot authentication va role authorization that cho backend.
Trong repo nay, de khoa route dung nghia thi phai hoan thien JWT truoc, nen giai doan nay duoc trien khai theo dung thu tu ky thuat.

## 1. Muc tieu cua giai doan 7

- Chot 3 role: `ADMIN`, `STAFF`, `USER`
- Phan biet ro `401` va `403`
- Phat va validate `JWT`
- Khong cho role thuong cham vao route quan tri
- Van de Swagger public de demo va test

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

## 4. Vi sao phai hoan thien auth that truoc khi khoa role

Neu auth chi la stub thi role authorization khong co gia tri that, vi:

- khong co token that de validate
- khong co principal that trong security context
- khong co user/role that trong database

Nen implementation di theo thu tu hop ly:

1. Tao `User` entity that.
2. Hash password bang bcrypt.
3. Tao `register/login/me`.
4. Phat JWT.
5. Dua user vao `SecurityContext`.
6. Sau do moi ap role rule.

## 5. `User` entity duoc thiet ke the nao

`User` implement `UserDetails`.

Y nghia:

- entity database co the di thang vao Spring Security
- `getAuthorities()` tra ve `ROLE_ADMIN`, `ROLE_STAFF`, `ROLE_USER`
- khong can tao adapter class vong vo

`passwordHash` duoc dat `@JsonIgnore` de tranh lo hash ra response.

## 6. JWT flow hoat dong ra sao

### `AuthService`

- `register`: tao user moi, role mac dinh `USER`, hash password bang bcrypt
- `login`: authenticate, tao JWT, tra `AuthResponse`
- `me`: doc principal hien tai de tra ve user dang dang nhap

### `JwtService`

Service nay:

- tao token
- doc email tu token
- kiem tra han
- validate token voi `UserDetails`

### `JwtAuthenticationFilter`

Filter nay:

1. Doc header `Authorization`
2. Cat bo prefix `Bearer `
3. Validate token
4. Load user tu DB
5. Dat `Authentication` vao `SecurityContext`

## 7. `401` va `403` duoc tach the nao

Trong `SecurityConfig`:

- `AuthenticationEntryPoint` xu ly `401`
- `AccessDeniedHandler` xu ly `403`

Y nghia:

- khong co token hoac token sai => `401`
- co token hop le nhung role khong du => `403`

Day la diem nguoi cham bai rat de hoi.

## 8. Demo user duoc sinh de lam gi

`DemoUserInitializer` tao san:

- `admin@securityapp.local`
- `staff@securityapp.local`
- `user@securityapp.local`

Tac dung:

- demo nhanh khong phai seed tay
- Postman va Swagger co account on dinh de test
- giam cong chuan bi moi lan chay lai he thong

## 9. Test cua giai doan 7 chung minh dieu gi

`AuthSecurityIntegrationTest` cover:

- register/login/me flow chay that
- token sai hoac thieu token bi `401`
- `STAFF` vao duoc customer API
- `USER` bi chan khoi customer API
- `ADMIN` vao duoc summary

## 10. Cach tom tat khi thuyet trinh

> Giai doan 7 cua em khong chi viet role rule, ma hoan thien luon JWT auth de role rule co gia tri that. He thong phan biet ro `401` va `403`, co 3 role `ADMIN`, `STAFF`, `USER`, va chi mo dung nhung route can thiet cho tung nhom nguoi dung.
