# Phần B – Quản lý đợt đăng ký và đề xuất đề tài

## B01 – Quản lý đợt đăng ký
- [x] Entity, repository, DTO, service và controller.
- [x] Danh sách, chi tiết, tạo, sửa và xóa đợt nháp; mở và đóng đợt.
- [x] Kiểm tra lịch, loại đợt, số thành viên và chuyển đổi giờ Việt Nam/UTC.
- [x] Chuyển ba trang quản lý đợt sang layout chung, bỏ tham chiếu navbar đã xóa.
- [x] Kiểm thử service và lưu dữ liệu MySQL.
- [ ] Xác nhận trực tiếp bố cục và thao tác trên trình duyệt.

## B02 – Đề xuất đề tài
- [x] GV/SV tạo, sửa nháp của mình và gửi DRAFT sang SUBMITTED.
- [x] Danh sách và chi tiết cá nhân; kiểm tra vai trò, chủ sở hữu, mã trùng và thời gian.
- [x] Khóa bản ghi và ràng buộc MySQL bảo vệ thao tác ghi.
- [x] Danh sách chọn đợt/bộ môn; giữ lựa chọn khi lỗi và khóa đợt khi sửa.
- [x] Menu đề xuất đề tài cho LECTURER/STUDENT.
- [x] Kiểm thử service, HTTP, CSRF, phân quyền và dữ liệu MySQL.
- [ ] Xác nhận trực tiếp bố cục và thao tác trên trình duyệt.

## B05 – Kiểm tra thời gian
- [x] Kiểm tra OPEN và khoảng [start, end) với UTC.
- [x] 12 bài kiểm thử RoundPolicy đạt.
- [x] Tích hợp vào tạo/sửa/gửi đề xuất B02.
- [ ] Kiểm chứng đầy đủ mốc thời gian qua MySQL cho các thao tác nhóm sinh viên.

## Giới hạn
- Hết hạn sẽ chặn thao tác; trạng thái CLOSED được đổi thủ công sau hạn đăng ký.
- Chưa kiểm thử tải hoặc mọi tình huống cập nhật đồng thời.
- B03 (duyệt/công bố) là phạm vi riêng, không thuộc luồng đề xuất B02.

## Chạy dự án (CMD)
```bat
set "SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/student_topic_management?sslMode=REQUIRED&connectionTimeZone=UTC"
set "SPRING_DATASOURCE_USERNAME=root"
set /p "SPRING_DATASOURCE_PASSWORD=Nhap mat khau MySQL: "
mvnw.cmd spring-boot:run
```
Trong cùng cửa sổ CMD, có thể dùng `mvnw.cmd test` để chạy kiểm thử với cấu hình trên.
Kiểm thử tích hợp cần MySQL có schema phù hợp; dữ liệu thử được rollback, AUTO_INCREMENT có thể tăng.

## Kết quả xác minh ngày 19/10/2026
- Toàn bộ 108 bài kiểm thử đạt, 0 failures, 0 errors, 0 skipped; Maven BUILD SUCCESS.
- Có kiểm thử tích hợp MySQL, đăng nhập GV/SV, quyền DEAN và CSRF cho trang quản lý đợt.
- Kiểm thử mới xác nhận lọc danh sách đợt và nạp lại các lựa chọn khi validation lỗi.
- Chưa xác minh bố cục trực tiếp trên trình duyệt và chưa kiểm thử tải.
