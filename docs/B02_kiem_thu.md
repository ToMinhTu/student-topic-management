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
2. Chọn bộ môn và đợt OPEN đang trong pha đề xuất; lưu nháp.
3. Xem chi tiết, sửa tên, gửi; xác nhận trạng thái SUBMITTED và không còn nút sửa.
4. Dùng tài khoản khác truy cập ID vừa tạo: không được xem/sửa/gửi.
5. Với nháp trong đợt đã đóng/hết hạn, gửi thất bại phải hiện thông báo.

MockMvc kiểm tra HTML phía server và luồng HTTP; không thay thế kiểm tra bố cục
trong trình duyệt. Luồng duyệt/công bố thuộc B03.

Kết quả ngày 17/10/2026: clean test thành công, 105 test đạt (41 test B02 mới), 0 lỗi và 0 bỏ qua.

## Bổ sung hoàn thiện B02
- POST /topics/{id}/delete: chỉ chủ sở hữu DRAFT, tài khoản hoạt động, trong pha đề xuất; CSRF bắt buộc.
- Có topic_supervisors hoặc topic_registrations thì khóa ngoại RESTRICT chặn xóa; giữ dữ liệu liên quan.
- Chủ sở hữu được sửa REJECTED thành DRAFT hoặc nộp lại thành SUBMITTED trong pha đề xuất.
- Khi mở lại REJECTED, lưu người thao tác, trạng thái và toàn bộ trường duyệt cũ vào audit_logs trước khi reset.
- Các trường review dài được chia phần, giữ nguyên Unicode để không vượt MySQL TEXT.
- Lịch sử tạo/sửa/nộp/xóa dùng audit_logs với entity_type TOPIC, trong cùng transaction; không thêm bảng mới.
- Mã được strip/chuyển chữ hoa Locale.ROOT, không nhận khoảng trắng bên trong/ký tự điều khiển.
- Kiểm tra mã trùng dùng upper(trim(code)) trong cùng đợt và bỏ qua chính ID đang sửa; unique MySQL vẫn bảo vệ bước lưu.
- Không thay đổi hàng loạt mã đã có trong database. Chưa có trang tra cứu audit riêng.

Kết quả hoàn thiện B02 ngày 19/10/2026: `mvnw.cmd clean test` đạt 132/132 bài, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS. Bốn mục xóa DRAFT, sửa/nộp lại REJECTED, reset/lưu lịch sử và chuẩn hóa/kiểm tra mã đã được triển khai và kiểm thử. Chưa xác nhận bố cục trực tiếp trên trình duyệt.
