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

# Phần A:
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


# Phần B:



## Phần C – Quản lý nhóm sinh viên

### 1. Quản lý nhóm sinh viên (groups):
- [x] C01.3 - Sinh viên tạo nhóm mới
- [x] Tự động thiết lập người tạo nhóm làm trưởng nhóm
- [x] Kiểm tra điều kiện tạo nhóm theo đợt đăng ký
- [x] C01.4 - Sinh viên tham gia nhóm
- [x] Kiểm tra số lượng thành viên tối đa
- [x] Không cho phép sinh viên tham gia đồng thời nhiều nhóm đang hoạt động trong cùng phạm vi đăng ký
- [x] Hiển thị danh sách nhóm và thông tin chi tiết nhóm

### 2. Quản lý thành viên nhóm:
- [x] C02.1 - Thành viên rời nhóm
- [x] Kiểm tra điều kiện rời nhóm
- [x] Lưu lịch sử thành viên rời nhóm
- [x] C02.2 - Chuyển quyền trưởng nhóm
- [x] Chỉ trưởng nhóm được phép chuyển quyền
- [x] Chỉ cho phép chuyển quyền cho thành viên đang hoạt động trong nhóm
- [x] Cập nhật trưởng nhóm và thứ tự thành viên
- [x] Kiểm tra điều kiện chuyển quyền theo trạng thái nhóm và đợt đăng ký

### 3. Giao diện:
- [x] Trang danh sách nhóm sinh viên
- [x] Trang tạo nhóm
- [x] Trang chi tiết nhóm
- [x] Chức năng tham gia, rời nhóm và chuyển quyền trưởng nhóm
- [x] Thêm đường dẫn Nhóm sinh viên vào navbar

### 4. Kiểm thử:
- [x] Kiểm thử nghiệp vụ bằng JUnit 5 và Mockito
- [x] Hoàn thành 16/16 bài kiểm thử GroupServiceTest
- [x] Kiểm tra chức năng trên giao diện Spring Boot
- [x] Kiểm tra kết quả thay đổi dữ liệu trong MySQL

### 5. Đường dẫn chức năng:
- Danh sách nhóm: `/groups`
- Tạo nhóm: `/groups/create`
- Chi tiết nhóm: `/groups/{id}`

### 6. Giới hạn hiện tại:
- Chưa hỗ trợ chuyển quyền trưởng nhóm khi cả 5 vị trí thành viên đang được sử dụng.

# Phần D:


### 1. Phần hội đồng thi (councils): ✔
- [x] Hiển thị danh sách hội đồng theo đợt đăng ký
- [x] Tạo hội đồng mới
- [x] Cho phép chỉnh sửa tên hội đồng và đợt đăng ký khi cần
- [x] Gán hội đồng với đợt đăng ký
- [x] Hiển thị trạng thái hội đồng
- [x] Quản lý danh sách thành viên hội đồng
- [x] Thêm giảng viên vào hội đồng
- [x] Gán vị trí thành viên: Chủ tịch, Thư ký, Ủy viên
- [x] Kiểm tra dữ liệu khi tạo và thêm thành viên
- [x] Phân quyền thao tác theo vai trò

### 2. Đồng bộ templates chức năng A2, B1:

### 3. Cách hoạt động:

Tài khoản:
| Role | Tài khoản | mật khẩu |
|:-------------|:--------------:|--------------:|
| ADMIN         |    admin      |         admin123 |
| SinhVien         |     test_student1       |          0586116321Test |
| SinhVien         |     test_student2       |          0586116321Tt |
| GiaoVien         |     test_teacher1       |          0586116321Tt |

- [x] Tạo đợt đăng ký ở phần (Quản lý đăng ký) role(giáo viên)
- [x] Qua hội đồng tạo mới, thêm id đợt đã tạo và thêm thành viên ủy viên
- [x] Đợt thi và hội đồng thi sẽ hiển thị ở phần (nhóm của tôi) role(sinh viên), nếu chưa thêm hội đồng thì sẽ hiển thị chỉ "đang sắp xếp hội đồng"