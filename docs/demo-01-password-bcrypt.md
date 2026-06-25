# Demo 01 - bcrypt password hashing

## Muc tieu

- Chung minh password trong database la hash bcrypt
- Login van thanh cong bang password goc
- Giai thich password khong the giai ma nguoc

## Cach demo

1. Dang ky user moi hoac dung user seed.
2. Query database:

```powershell
docker exec securityapp-db mysql -uroot -p<MYSQL_ROOT_PASSWORD> securityapp -e "SELECT id,email,password_hash,role FROM users;"
```

3. Kiem tra `password_hash` co prefix `$2`.
4. Dang nhap lai bang password goc.

## Ket luan

- bcrypt la one-way hashing
- bcrypt tu sinh salt
- hash trong DB khong phai plaintext
