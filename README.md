# student-topic-management

[FIle EXCEL Tổng hợp](https://docs.google.com/spreadsheets/d/15j5iLNbXE_Gw3CXLfULijvWwq__4noOaoX7NG0bpk4o/edit?usp=sharing)

> [!IMPORTANT]
> Dùng edge thì chạy link `127.0.0.1:8080/` thay cho `localhost`

>[!NOTE]
> Nếu build bị lỗi entity thì chạy lệnh `mvn clean` -> `mvn spring-boot:run`

Tài khoản:
| Role | Tài khoản | mật khẩu |
|:-------------|:--------------:|--------------:|
| ADMIN         |    admin      |         admin123 |
| text         |     text       |          text |

[Link Test](http://127.0.0.1:8080/)

### 1. Phần bộ môn (departments):
- [✔] thêm mới
- [✔] Sửa
- [✔] Xoá
- [✔] Báo lỗi khi tên trùng với bộ môn đã có sẵn
- [✔] Báo lỗi khi bộ môn có tài khoản trực thuộc

### 2. Chia quyền đăng nhập theo role: ✔
	
### 3. Phần quản lý người dùng (users):
- [✔] Màn hình quản lý
- [✔] Tính năng khoá người dùng - đồng thời logout các session đăng nhập khác
- [✔] Không khoá người dùng ADMIN cuối cùng
- [✔] Xoá người dùng: Chưa làm
	
### 4. Phần thêm sinh viên (students):
- [✔] validate các trường dữ liệu
- [✔] Không tạo trùng username
- [✔] Không tạo trùng email
- [✔] Không tạo trùng mã số sinh viên
- [✔] Tạo sinh viên
	
### 5. Phần thêm giảng viên (lecturers):
- [✔] validate các trường dữ liệu
- [✔] Không tạo trùng username
- [✔] Không tạo trùng email
- [✔] Không tạo trùng mã số giảng viên
- [✔] Tạo giảng viên

### 6. Phần thông báo (announcements):
- Danh sách quản lý thông báo (Admin/Dean)
    - [✔] Thêm thông báo (Nháp)
    - [✔] validate
    - [✔] Sửa
    - [✔] Xoá
    - [✔] Công bố
    - [✔] Sau khi công bố thì thông báo biến mất ở danh sách quản lý (Không cho phép thay đổi thông báo đã công bố)
- Thông báo cho student/lecturer:
    - [✔] Hiện danh sách
    - [✔] Đọc chi tiết
    - [✔] Hiện lỗi khi vào bằng url với thông báo chưa được publish