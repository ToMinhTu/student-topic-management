<!-- Thanh dieu huong -->
| [📄 README](./README.md) | [📋 TASKS A](./TASKS_A.md) | [📋 TASKS B](./TASKS_B.md) | [📋 TASKS C](./TASKS_C.md) | [📋 TASKS D](./TASKS_D.md) |
| :--- | :--- | :--- | :--- | :--- |

# Phần A:
## ⚙ Cấu hình môi trường:
- [x] application
- [x] application-dev
- [x] application-prod
- [x] application-mail
- [x] env

## 1. Phần bộ môn (departments):
- [x] thêm mới
- [x] Sửa
- [x] Xoá
- [x] Báo lỗi khi tên trùng với bộ môn đã có sẵn
- [x] Báo lỗi khi bộ môn có tài khoản trực thuộc

## 2. Account profile (AppUser):
- [x] Chia quyền đăng nhập theo role
- [x] Nhấn nút back sau khi login thành công thì luôn quay vể trang homepage
- [x] Xem info user
- [x] Đổi password
- [x] validate
- [x] Thu hồi session sau khi đổi password
- [x] Thu hồi session sau khi tài khoản bị lock
	
## 3. Phần quản lý người dùng (users):
- [x] Màn hình quản lý
- [x] Tính năng khoá người dùng - đồng thời logout các session đăng nhập khác
- [x] Không khoá người dùng ADMIN cuối cùng
- [x] Xoá người dùng: Chưa làm
	
## 4. Phần thêm sinh viên (students):
- [x] validate các trường dữ liệu
- [x] Không tạo trùng username
- [x] Không tạo trùng email
- [x] Không tạo trùng mã số sinh viên
- [x] Tạo sinh viên
	
## 5. Phần thêm giảng viên (lecturers):
- [x] validate các trường dữ liệu
- [x] Không tạo trùng username
- [x] Không tạo trùng email
- [x] Không tạo trùng mã số giảng viên
- [x] Tạo giảng viên
- [x] Tạo giảng viên với các role bổ sung

## 6. Phần thông báo (announcements):
- Danh sách quản lý thông báo (Admin/Dean)
    - [x] Thêm thông báo (Nháp)
    - [x] validate
    - [x] Sửa
    - [x] Xoá
    - [x] Công bố
    - [x] Sau khi công bố thì thông báo biến mất ở danh sách quản lý (Không cho phép thay đổi thông báo đã công bố)
- Thông báo cho student/lecturer:
    - [x] Hiện danh sách
    - [x] Đọc chi tiết
    - [x] Hiện lỗi khi vào bằng url với thông báo chưa được publish
- Chống bảo mật HTML chống XSS: ✔

## 7. Phần hệ thống nhật ký (Audit Log):
- [x] Hiển thị danh sách
- [x] Lưu nhật ký khi thực hiện lưu Nháp/Công bố thông báo
- [x] Luôn ghi log kể cả khi thao tác fail

## 8. Phần Email Outbox:
- [x] Hiển thị danh sách
- [x] ghi nhận các yêu cầu gửi email (khi công bố điểm/đề tài)

## 9. Giao diện:
- [x] layout
- [x] header
- [x] sidebar
- [x] messages
- [x] loading
- [x] login
- [x] homepage
- [x] access-denied
- [x] 404