# Demo 03 - AES-GCM encryption

## Muc tieu

- Du lieu nhay cam trong DB duoc luu duoi dang ma hoa
- Backend giai ma khi tra ve cho user hop le
- Sua ciphertext se lam decrypt fail

## Du lieu duoc ma hoa

- `users.phone_number_encrypted`
- `users.billing_address_encrypted`
- `enrollments.payment_reference_encrypted`
- `certificates.certificate_code_encrypted`

## Cach demo

1. Dang nhap `student1@example.com`
2. Goi `GET /api/auth/me`
3. Query DB:

```powershell
docker exec securityapp-db mysql -uroot -p<MYSQL_ROOT_PASSWORD> securityapp -e "SELECT id,email,phone_number_encrypted,billing_address_encrypted FROM users;"
```

4. So sanh response API voi ciphertext trong DB.
5. Chay unit test `EncryptionServiceTest` de thay truong hop tampered ciphertext.

## Ket luan

- AES-GCM bao mat bi mat va toan ven
- Moi lan encrypt dung IV ngau nhien
