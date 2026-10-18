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


