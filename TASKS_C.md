<!-- Thanh dieu huong -->
| [📄 README](./README.md) | [📋 TASKS A](./TASKS_A.md) | [📋 TASKS B](./TASKS_B.md) | [📋 TASKS C](./TASKS_C.md) | [📋 TASKS D](./TASKS_D.md) |
| :--- | :--- | :--- | :--- | :--- |

# Phần C – Quản lý nhóm sinh viên

## 1. Quản lý nhóm sinh viên (groups):
- [x] C01.3 - Sinh viên tạo nhóm mới
- [x] Tự động thiết lập người tạo nhóm làm trưởng nhóm
- [x] Kiểm tra điều kiện tạo nhóm theo đợt đăng ký
- [x] C01.4 - Sinh viên tham gia nhóm
- [x] Kiểm tra số lượng thành viên tối đa
- [x] Không cho phép sinh viên tham gia đồng thời nhiều nhóm đang hoạt động trong cùng phạm vi đăng ký
- [x] Hiển thị danh sách nhóm và thông tin chi tiết nhóm

## 2. Quản lý thành viên nhóm:
- [x] C02.1 - Thành viên rời nhóm
- [x] Kiểm tra điều kiện rời nhóm
- [x] Lưu lịch sử thành viên rời nhóm
- [x] C02.2 - Chuyển quyền trưởng nhóm
- [x] Chỉ trưởng nhóm được phép chuyển quyền
- [x] Chỉ cho phép chuyển quyền cho thành viên đang hoạt động trong nhóm
- [x] Cập nhật trưởng nhóm và thứ tự thành viên
- [x] Kiểm tra điều kiện chuyển quyền theo trạng thái nhóm và đợt đăng ký

## 3. Giao diện:
- [x] Trang danh sách nhóm sinh viên
- [x] Trang tạo nhóm
- [x] Trang chi tiết nhóm
- [x] Chức năng tham gia nhóm
- [x] Chức năng rời nhóm
- [x] Chức năng chuyển quyền trưởng nhóm
- [x] Thêm đường dẫn Nhóm sinh viên vào navbar

## 4. Kiểm thử:
- [x] Kiểm thử nghiệp vụ bằng JUnit 5 và Mockito
- [x] Hoàn thành 16/16 bài kiểm thử GroupServiceTest
- [x] Kiểm tra chức năng trên giao diện Spring Boot
- [x] Kiểm tra kết quả thay đổi dữ liệu trong MySQL

## 5. Đường dẫn chức năng:
- Danh sách nhóm: `/groups`
- Tạo nhóm: `/groups/create`
- Chi tiết nhóm: `/groups/{id}`

## 6. Giới hạn hiện tại:
- Chưa hỗ trợ chuyển quyền trưởng nhóm khi cả 5 vị trí thành viên đang được sử dụng.

## 7. C03 – Đăng ký đề tài cho nhóm sinh viên

### C03.1 – Xem danh sách đề tài đã công bố
- [x] Hiển thị danh sách đề tài theo đợt đăng ký
- [x] Chỉ hiển thị đề tài có trạng thái PUBLISHED
- [x] Hiển thị mã đề tài, tên, bộ môn, mô tả và yêu cầu
- [x] Hiển thị trạng thái còn trống hoặc đã được đăng ký
- [x] Tích hợp giao diện sidebar và layout chung của hệ thống

### C03.2 – Trưởng nhóm đăng ký đề tài
- [x] Chỉ trưởng nhóm sinh viên được đăng ký
- [x] Kiểm tra trạng thái và thời hạn đợt đăng ký
- [x] Kiểm tra nhóm đủ số lượng thành viên
- [x] Chỉ cho phép đăng ký đề tài đã công bố
- [x] Ngăn nhóm đăng ký đề tài nhiều lần
- [x] Ngăn nhiều nhóm giữ chỗ cùng một đề tài
- [x] Lưu đăng ký vào bảng topic_registrations
- [x] Thiết lập trạng thái đăng ký ban đầu là PENDING
- [x] Hiển thị nút Đăng ký cho trưởng nhóm đủ điều kiện

### Kiểm thử C03
- [x] Kiểm thử đăng ký đề tài trên giao diện Spring Boot
- [x] Xác nhận bản ghi PENDING được lưu vào MySQL
- [x] Hoàn thành 5/5 bài kiểm thử TopicRegistrationServiceTest
- [ ] Kiểm thử đầy đủ các tình huống tích hợp và đăng ký đồng thời

### Đường dẫn chức năng
- Danh sách đề tài: GET /topics?roundId={roundId}
- Đăng ký đề tài: POST /topics/{topicId}/register

### Giới hạn hiện tại
- Mỗi nhóm chỉ được có một bản ghi đăng ký đề tài theo ràng buộc UNIQUE(group_id) của cơ sở dữ liệu.
- Chưa triển khai quy trình hủy đăng ký và đăng ký lại đề tài.
- Chưa hoàn thiện toàn bộ quy trình xử lý đăng ký đề tài sau khi gửi yêu cầu PENDING.

### Ghi chú kiểm thử toàn dự án
- Unit Test C03: 5/5 PASS.
- Bộ kiểm thử toàn dự án: 69 bài, 6 lỗi RoundControllerTest do các trang rounds còn tham chiếu fragments/navbar.html đã bị thay thế trong giao diện mới.
- Cần cập nhật các trang cũ sang layout chung và chạy lại toàn bộ kiểm thử.
