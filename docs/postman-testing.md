# Postman testing guide

## 1. Import collection

- Import `postman/online-course-security.postman_collection.json`
- Dat `baseUrl = https://localhost`
- Dat `webhookSecret` bang gia tri `HMAC_WEBHOOK_SECRET` trong `.env`

## 2. Dang ky student moi

- Chay request `Register`
- Ky vong `201 Created`
- Response co `accessToken`, `refreshToken`, `user.role = STUDENT`

## 3. Dang nhap lay JWT

- Chay `Login Student`
- Collection se luu `accessToken`, `refreshToken`, `studentUserId`
- Chay `Login Admin` neu muon test admin APIs

## 4. Goi API bang Bearer token

- Chay `Get Courses`
- Chay `Checkout Course`
- Chay `Get Lesson With Token`
- Chay `Get My Certificate`

## 5. Test token sai

- Dang nhap lay JWT
- Sua payload JWT bang tay trong tab raw token hoac tao 1 request moi voi token da bi tamper
- Goi `GET /api/auth/me`
- Ky vong `401` va thong bao chu ky JWT khong hop le hoac token het han

## 6. Test BOLA / IDOR

- Student A login, lay `certificateId` cua chinh minh
- Student B login
- Dat `victimCertificateId` = `certificateId` cua Student A
- Chay `BOLA Attack Attempt`
- Ky vong `403 Forbidden`
- Sau do login admin va chay `Admin Audit Logs` de xem log `ACCESS_DENIED`

## 7. Test webhook HMAC

- Tao hoac dung enrollment `PENDING`
- Chay `Webhook Valid HMAC`
- Ky vong `200`, enrollment duoc ACTIVE, certificate duoc issue
- Chay `Webhook Invalid HMAC`
- Ky vong `401`
- Chay lai `Webhook Valid HMAC` voi cung `eventId`
- Ky vong `409 Conflict`

## 8. Test rate limit

- Chay `Rate Limit Test - Wrong Login` nhieu lan lien tiep
- Ky vong sau mot nguong se nhan `429 Too Many Requests`
- Xem audit log hoac console backend de thay `RATE_LIMIT_EXCEEDED`
