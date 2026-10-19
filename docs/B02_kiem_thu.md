# Kiểm thử B02 – Đề xuất đề tài

Chạy trên Java 21 và MySQL đã khởi tạo theo schema_mysql.sql.
Cấu hình SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME và
SPRING_DATASOURCE_PASSWORD trong phiên terminal trước khi chạy Maven;
không ghi mật khẩu vào repository.

Chạy toàn bộ: ./mvnw.cmd clean test
Chạy riêng B02: ./mvnw.cmd "-Dtest=TopicProposalServiceTest,TopicProposalControllerTest,TopicProposalErrorTest" test

## Phạm vi

- Service: vai trò, tài khoản bị khóa, chủ sở hữu, DRAFT/SUBMITTED,
  mã trùng khi tạo/sửa, ID và dữ liệu không hợp lệ, bộ môn/đợt không tồn tại,
  giới hạn byte của TEXT, kiểm tra [start, end) bằng Clock cố định.
- Controller tích hợp: đăng nhập thật qua Spring Security với GV và SV,
  render ba trang Thymeleaf, tạo/sửa/gửi qua MockMvc và xác nhận dữ liệu MySQL.
- Bảo mật: chưa đăng nhập, sai vai trò, thiếu/sai CSRF ở cả ba URL POST,
  không giả mạo proposedBy/status, không xem/sửa/gửi đề tài của người khác,
  không sửa/gửi lại sau SUBMITTED, escape nội dung người dùng.
- Lỗi: hết hạn/đóng đợt, validation, mã trùng, đề tài không tồn tại,
  giữ dữ liệu form và hiển thị thông báo an toàn khi database từ chối lưu.
- MySQL: kiểm tra trực tiếp unique và khóa ngoại bằng SQL.

TopicProposalControllerTest tạo tài khoản và dữ liệu riêng trong transaction.
Sau mỗi test rollback, AfterTransaction xác nhận không còn các bản ghi thử.
Các test SQL dùng chung cấu hình datasource của dự án; cần quyền truy cập MySQL.
AUTO_INCREMENT có thể tăng dù transaction rollback.

## Đồng thời

Các thao tác ghi khóa đợt bằng findByIdForUpdate trong cùng transaction,
đồng bộ với mở/đóng đợt B01; sửa/gửi khóa thêm bản ghi đề tài.
Unique (round_id, code) vẫn là lớp bảo vệ cuối cùng.
Bộ test này chưa phải kiểm thử tải hoặc kiểm chứng mọi lịch xen kẽ đồng thời.

## Kiểm tra thủ công

1. Đăng nhập GV/SV; mở /topics/my rồi /topics/new.
2. Dùng ID bộ môn và đợt OPEN đang trong pha đề xuất; lưu nháp.
3. Xem chi tiết, sửa tên, gửi; xác nhận trạng thái SUBMITTED và không còn nút sửa.
4. Dùng tài khoản khác truy cập ID vừa tạo: không được xem/sửa/gửi.
5. Với nháp trong đợt đã đóng/hết hạn, gửi thất bại phải hiện thông báo.

MockMvc kiểm tra HTML phía server và luồng HTTP; không thay thế kiểm tra bố cục
trong trình duyệt. Luồng duyệt/công bố thuộc B03.

Kết quả ngày 17/10/2026: clean test thành công, 105 test đạt (41 test B02 mới), 0 lỗi và 0 bỏ qua.
