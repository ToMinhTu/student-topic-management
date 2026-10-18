# Phần A:
### ⚙ Cấu hình môi trường:
- [x] application: ✔
- [x] application-dev: ✔
- [x] application-prod: ✔
- [x] env: ✔
### 1. Phần bộ môn (departments):
- [x] thêm mới
- [x] Sửa
- [x] Xoá
- [x] Báo lỗi khi tên trùng với bộ môn đã có sẵn
- [x] Báo lỗi khi bộ môn có tài khoản trực thuộc

### 2. Chia quyền đăng nhập theo role: ✔
### 3. Out session khi đổi mật khẩu: ✔
	
### 4. Phần quản lý người dùng (users):
- [x] Màn hình quản lý
- [x] Tính năng khoá người dùng - đồng thời logout các session đăng nhập khác
- [x] Không khoá người dùng ADMIN cuối cùng
- [x] Xoá người dùng: Chưa làm
	
### 5. Phần thêm sinh viên (students):
- [x] validate các trường dữ liệu
- [x] Không tạo trùng username
- [x] Không tạo trùng email
- [x] Không tạo trùng mã số sinh viên
- [x] Tạo sinh viên
	
### 6. Phần thêm giảng viên (lecturers):
- [x] validate các trường dữ liệu
- [x] Không tạo trùng username
- [x] Không tạo trùng email
- [x] Không tạo trùng mã số giảng viên
- [x] Tạo giảng viên

### 7. Phần thông báo (announcements):
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