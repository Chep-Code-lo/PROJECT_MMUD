# Caption và mô tả ảnh cho báo cáo

Tài liệu này dùng để copy trực tiếp caption và phần mô tả ngắn cho các ảnh chụp màn hình trong báo cáo đồ án.

## Hình 1. Giao diện tổng quan của hệ thống

**Caption**  
Hình 1. Giao diện tổng quan của hệ thống khóa học online nhỏ chạy qua giao thức HTTPS.

**Mô tả gợi ý**  
Hình ảnh minh họa giao diện frontend NextJS của hệ thống khi được truy cập qua `https://localhost`. Đây là điểm vào để người dùng thực hiện các thao tác đăng nhập, xem khóa học, ghi danh khóa học và demo các cơ chế bảo mật được tích hợp trong đồ án.

## Hình 2. Giao diện Swagger/OpenAPI

**Caption**  
Hình 2. Giao diện Swagger UI hỗ trợ kiểm thử các RESTful API của hệ thống.

**Mô tả gợi ý**  
Swagger UI được cấu hình tại `https://localhost/swagger-ui.html`, cho phép mô tả cấu trúc API, request body, response và mã lỗi. Đây là công cụ quan trọng để kiểm thử JWT, phân quyền, webhook HMAC và các endpoint bảo mật mà không cần viết thêm giao diện.

## Hình 3. Đăng nhập thành công và nhận JWT

**Caption**  
Hình 3. Kết quả đăng nhập thành công và hệ thống trả về access token và refresh token.

**Mô tả gợi ý**  
Sau khi người dùng đăng nhập đúng email và mật khẩu, backend Spring Boot trả về `accessToken`, `refreshToken`, `tokenType`, `role` và `scope`. Hình ảnh này dùng để chứng minh hệ thống đã áp dụng JWT cho xác thực và sử dụng mô hình Bearer Token trong REST API.

## Hình 4. Gọi endpoint bảo vệ bằng Bearer token

**Caption**  
Hình 4. Truy cập endpoint bảo vệ thành công bằng Bearer JWT.

**Mô tả gợi ý**  
Sau khi đăng nhập, client gửi `Authorization: Bearer <access_token>` để truy cập endpoint `GET /api/auth/me`. Kết quả thành công cho thấy backend đã xác minh chữ ký token, thời gian hết hạn và role của người dùng trước khi cấp quyền truy cập tài nguyên.

## Hình 5. Mật khẩu được lưu dưới dạng bcrypt hash

**Caption**  
Hình 5. Dữ liệu mật khẩu được lưu trong cơ sở dữ liệu dưới dạng bcrypt hash.

**Mô tả gợi ý**  
Trong bảng `users`, trường `password_hash` không lưu mật khẩu gốc mà lưu chuỗi bcrypt bắt đầu bằng `$2a$` hoặc `$2b$`. Hình này chứng minh hệ thống không lưu plaintext password, phù hợp với yêu cầu hashing một chiều trong môn Mật mã ứng dụng.

## Hình 6. Dữ liệu nhạy cảm của người dùng được mã hóa AES-GCM

**Caption**  
Hình 6. Các trường thông tin nhạy cảm của người dùng được mã hóa bằng AES-GCM trong cơ sở dữ liệu.

**Mô tả gợi ý**  
Hai trường `phone_number_encrypted` và `billing_address_encrypted` trong bảng `users` được lưu dưới dạng ciphertext thay vì plaintext. Hình này được dùng để chứng minh hệ thống áp dụng mã hóa đối xứng AES-GCM để bảo vệ dữ liệu nhạy cảm khi lưu trữ.

## Hình 7. Danh sách chứng chỉ của chính người dùng

**Caption**  
Hình 7. Người dùng chỉ xem được danh sách chứng chỉ của chính mình.

**Mô tả gợi ý**  
Khi đăng nhập bằng tài khoản `student1`, endpoint `GET /api/certificates/me` trả về danh sách chứng chỉ thuộc sở hữu của tài khoản này. Hình này là cơ sở để thực hiện tiếp kịch bản tấn công BOLA/IDOR ở các hình sau.

## Hình 8. Tấn công BOLA/IDOR vào chứng chỉ của người khác

**Caption**  
Hình 8. Mô phỏng tấn công BOLA/IDOR bằng cách thay đổi `certificateId` trên URL.

**Mô tả gợi ý**  
Người dùng `student2` cố gắng gọi `GET /api/certificates/{certificateId}` với `certificateId` thuộc về `student1`. Hình ảnh này mô tả tình huống tấn công Broken Object Level Authorization khi đối tượng xấu lợi dụng định danh tài nguyên để truy cập dữ liệu không thuộc quyền sở hữu.

## Hình 9. Hệ thống chặn BOLA/IDOR bằng mã lỗi 403

**Caption**  
Hình 9. Backend từ chối truy cập trái phép vào chứng chỉ của người khác với mã lỗi 403 Forbidden.

**Mô tả gợi ý**  
Kết quả trả về `403 Forbidden` cho thấy việc kiểm tra ownership được thực hiện tại phía server thay vì chỉ ẩn nút trên giao diện. Đây là minh chứng rõ ràng rằng hệ thống đã phòng chống BOLA/IDOR đúng hướng của OWASP API Security Top 10.

## Hình 10. Bài học bị khóa trước khi ghi danh

**Caption**  
Hình 10. Người dùng chưa ghi danh không được phép xem nội dung bài học bị khóa.

**Mô tả gợi ý**  
Khi người dùng chưa có enrollment hợp lệ, endpoint `GET /api/courses/{courseId}/lessons/{lessonId}` trả về `403`. Hình này thể hiện cơ chế kiểm soát truy cập vào tài nguyên học tập dựa trên trạng thái ghi danh của người dùng.

## Hình 11. Tạo giao dịch checkout mô phỏng

**Caption**  
Hình 11. Hệ thống tạo enrollment ở trạng thái `PENDING` khi người dùng thực hiện checkout.

**Mô tả gợi ý**  
Endpoint `POST /api/courses/{courseId}/checkout` tạo một enrollment cho khóa học đang mua và sinh ra `paymentReference`, `suggestedEventId` và `timestamp` để phục vụ webhook thanh toán mô phỏng. Đây là dữ liệu đầu vào cho phần demo HMAC-SHA256.

## Hình 12. Webhook thanh toán sai chữ ký HMAC

**Caption**  
Hình 12. Webhook thanh toán bị từ chối khi chữ ký HMAC-SHA256 không hợp lệ.

**Mô tả gợi ý**  
Khi body request bị ký sai bằng secret không đúng hoặc bị sửa dữ liệu sau khi ký, endpoint `POST /api/webhooks/payment-success` trả về `401`. Hình này chứng minh HMAC được sử dụng để xác minh tính xác thực và tính toàn vẹn của webhook.

## Hình 13. Webhook thanh toán hợp lệ

**Caption**  
Hình 13. Webhook hợp lệ được chấp nhận và hệ thống kích hoạt enrollment.

**Mô tả gợi ý**  
Khi request webhook có `X-Signature`, `X-Timestamp` và `X-Event-Id` hợp lệ, backend chấp nhận thanh toán, chuyển enrollment sang trạng thái `ACTIVE` và phát hành chứng chỉ. Hình này là minh chứng cho việc áp dụng HMAC-SHA256 trong bài toán xác thực callback từ cổng thanh toán.

## Hình 14. Webhook replay bị từ chối

**Caption**  
Hình 14. Cơ chế chống replay attack đối với webhook thanh toán.

**Mô tả gợi ý**  
Nếu cùng một `X-Event-Id` được gửi lại lần thứ hai, hệ thống trả về `409 Conflict`. Hình này cho thấy backend đã lưu vết sự kiện webhook đã xử lý để ngăn chặn replay attack, bổ sung cho cơ chế xác minh HMAC.

## Hình 15. Bài học được mở khóa sau khi thanh toán thành công

**Caption**  
Hình 15. Người dùng xem được nội dung bài học sau khi enrollment được kích hoạt.

**Mô tả gợi ý**  
Sau khi webhook hợp lệ được xử lý, endpoint xem bài học trả về `200 OK` và có đầy đủ trường `content`. Hình này chứng minh logic phân quyền đã thay đổi đúng theo trạng thái nghiệp vụ và chỉ mở khóa tài nguyên sau khi thanh toán hợp lệ.

## Hình 16. Payment reference được mã hóa trong cơ sở dữ liệu

**Caption**  
Hình 16. Trường `payment_reference_encrypted` được lưu dưới dạng ciphertext trong bảng `enrollments`.

**Mô tả gợi ý**  
Sau khi checkout hoặc webhook được xử lý, `paymentReference` không được lưu dưới dạng thường mà được mã hóa bằng AES-GCM. Hình này minh họa việc hệ thống bảo vệ thông tin giao dịch và kết hợp mã hóa đối xứng với nghiệp vụ thanh toán.

## Hình 17. Certificate code được mã hóa trong cơ sở dữ liệu

**Caption**  
Hình 17. Trường `certificate_code_encrypted` được lưu dưới dạng mã hóa trong bảng `certificates`.

**Mô tả gợi ý**  
Mã chứng chỉ là dữ liệu nhạy cảm liên quan đến kết quả học tập của người dùng, do đó được mã hóa trước khi lưu trữ. Hình này kết hợp với kết quả API để cho thấy backend có thể giải mã đúng đối tượng hợp lệ nhưng dữ liệu trong database không tồn tại ở dạng plaintext.

## Hình 18. JWT bị sửa payload hoặc chữ ký

**Caption**  
Hình 18. Request bị từ chối khi JWT đã bị sửa đổi payload hoặc chữ ký.

**Mô tả gợi ý**  
Khi access token bị can thiệp thủ công, backend trả về `401 Unauthorized` vì chữ ký JWT không còn hợp lệ. Hình này được dùng để giải thích tính toàn vẹn của JWT và vai trò của secret key trong quá trình xác minh token.

## Hình 19. JWT hết hạn bị từ chối

**Caption**  
Hình 19. Hệ thống từ chối JWT đã hết hạn để giảm nguy cơ lợi dụng token cũ.

**Mô tả gợi ý**  
Token có trường `exp` và chỉ có hiệu lực trong một khoảng thời gian ngắn. Khi gửi access token đã hết hạn, backend phản hồi `401`, qua đó chứng minh hệ thống đang áp dụng cơ chế giới hạn thời gian sống của token đúng theo yêu cầu bảo mật.

## Hình 20. Sinh viên không được truy cập API quản trị

**Caption**  
Hình 20. Tài khoản sinh viên bị chặn khi truy cập endpoint quản trị.

**Mô tả gợi ý**  
Khi sử dụng token của `STUDENT` để gọi `GET /api/admin/audit-logs`, hệ thống trả về `403 Forbidden`. Hình này minh họa cơ chế role-based access control, đảm bảo chỉ tài khoản `ADMIN` mới có thể truy cập dữ liệu giám sát bảo mật.

## Hình 21. Quản trị viên xem audit log

**Caption**  
Hình 21. Tài khoản quản trị viên truy cập thành công danh sách audit log.

**Mô tả gợi ý**  
Khi đăng nhập bằng tài khoản `ADMIN`, endpoint `GET /api/admin/audit-logs` trả về danh sách các sự kiện an ninh của hệ thống. Đây là minh chứng cho khả năng giám sát, truy vết và phân tích hành vi bất thường trong hệ thống.

## Hình 22. Audit log ghi nhận hành vi bất thường

**Caption**  
Hình 22. Audit log ghi nhận các sự kiện `ACCESS_DENIED`, `TOKEN_REJECTED`, `WEBHOOK_REJECTED` và `RATE_LIMIT_EXCEEDED`.

**Mô tả gợi ý**  
Hình ảnh này cho thấy hệ thống không chỉ chặn request trái phép mà còn lưu vết đầy đủ để phục vụ điều tra và báo cáo. Đây là thành phần quan trọng trong kiến trúc phòng thủ theo chiều sâu của hệ thống.

## Hình 23. Rate limiting đối với chức năng đăng nhập

**Caption**  
Hình 23. Hệ thống giới hạn số lần đăng nhập sai và trả về mã lỗi 429 Too Many Requests.

**Mô tả gợi ý**  
Sau nhiều lần gọi liên tiếp với thông tin đăng nhập sai, endpoint `POST /api/auth/login` trả về `429`. Hình này được dùng để chứng minh hệ thống có cơ chế giảm thiểu tấn công brute-force và spam API.

## Hình 24. Chuyển hướng từ HTTP sang HTTPS

**Caption**  
Hình 24. Reverse proxy Nginx thực hiện chuyển hướng từ HTTP sang HTTPS.

**Mô tả gợi ý**  
Lệnh `curl -I http://localhost` trả về `301 Moved Permanently`, cho thấy tất cả request HTTP đều được buộc chuyển sang kênh HTTPS. Hình này minh họa vai trò của TLS trong việc bảo vệ dữ liệu khi truyền trên mạng.

## Hình 25. Kiểm tra sức khỏe hệ thống qua HTTPS

**Caption**  
Hình 25. Endpoint kiểm tra sức khỏe hệ thống hoạt động thành công trên kênh HTTPS.

**Mô tả gợi ý**  
Khi gọi `https://localhost/api/health`, hệ thống trả về trạng thái `UP`. Hình này cho thấy backend vẫn hoạt động ổn định khi đặt sau reverse proxy TLS và sẵn sàng phục vụ các kịch bản demo bảo mật.

## Hình 26. Kết quả quét OWASP ZAP baseline

**Caption**  
Hình 26. Kết quả quét OWASP ZAP baseline đối với bề mặt tấn công của hệ thống.

**Mô tả gợi ý**  
Báo cáo ZAP tại `docs/security/zap-baseline-report.html` cho thấy hệ thống không có `FAIL`, có `2 WARN` và `59 PASS`. Hình này được đưa vào để chứng minh đồ án đã được kiểm thử cơ bản bằng công cụ đánh giá bảo mật phổ biến, đồng thời giải trình các cảnh báo còn lại liên quan chủ yếu đến Swagger UI và cấu hình CSP.

## Gợi ý sắp xếp hình trong báo cáo

1. Giao diện và kiến trúc: Hình 1, Hình 2.
2. Xác thực và token: Hình 3, Hình 4, Hình 18, Hình 19.
3. Bảo vệ mật khẩu và mã hóa dữ liệu: Hình 5, Hình 6, Hình 16, Hình 17.
4. Phân quyền và chống BOLA/IDOR: Hình 7, Hình 8, Hình 9, Hình 10, Hình 15, Hình 20.
5. Webhook HMAC và thanh toán: Hình 11, Hình 12, Hình 13, Hình 14.
6. Audit log và rate limit: Hình 21, Hình 22, Hình 23.
7. TLS và kiểm thử bảo mật: Hình 24, Hình 25, Hình 26.
