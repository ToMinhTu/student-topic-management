# Phần B – Quản lý đợt đăng ký và đề xuất đề tài

<!-- Thanh điều hướng -->
| [📄 README](./README.md) | [📋 TASKS A](./TASKS_A.md) | [📋 TASKS B](./TASKS_B.md) | [📋 TASKS C](./TASKS_C.md) | [📋 TASKS D](./TASKS_D.md) |
| :--- | :--- | :--- | :--- | :--- |

## 1. Quản lý đợt đăng ký (B01)
- [x] Xây dựng Entity, Repository, DTO, Service và Controller
- [x] Tạo, chỉnh sửa, xem danh sách và chi tiết đợt đăng ký
- [x] Kiểm tra dữ liệu đầu vào và các điều kiện nghiệp vụ
- [x] Xây dựng giao diện quản lý đợt đăng ký bằng Thymeleaf
- [ ] Kiểm thử tích hợp phân quyền DEAN thực tế

## 2. Đề xuất đề tài (B02)
- [x] Xây dựng `Topic`, `TopicStatus`, Repository, DTO, Service và Controller
- [x] Giảng viên và sinh viên tạo đề xuất nháp
- [x] Chỉ chủ sở hữu được chỉnh sửa đề tài ở trạng thái `DRAFT`
- [x] Gửi đề xuất, chuyển trạng thái `DRAFT` sang `SUBMITTED`
- [x] Kiểm tra mã đề tài trùng, quyền người dùng và thời gian đề xuất
- [x] Sử dụng khóa `PESSIMISTIC_WRITE` khi cập nhật đề tài
- [x] Xem danh sách và chi tiết đề tài của mình
- [ ] Kiểm thử đầy đủ nghiệp vụ B02 và xử lý lỗi

## 3. Kiểm tra thời gian đăng ký (B05)
- [x] Xây dựng `RoundPolicy` kiểm tra trạng thái và thời gian đăng ký
- [x] Kiểm tra thời gian đề xuất đề tài và đăng ký sinh viên
- [x] Áp dụng khoảng thời gian `[start, end)` và UTC
- [x] Tích hợp B05 vào nghiệp vụ đề xuất đề tài B02
- [x] Hoàn thành 12/12 bài kiểm thử `RoundPolicyTest`
- [ ] Kiểm chứng tích hợp đầy đủ với các chức năng khác

## 4. Giao diện
- [x] Trang danh sách, tạo, chỉnh sửa và chi tiết đợt đăng ký
- [x] Trang danh sách, tạo, chỉnh sửa và chi tiết đề tài
- [x] Chức năng gửi đề xuất để xét duyệt
- [ ] Kiểm thử giao diện và hoàn thiện thông báo lỗi

## 5. Kiểm thử
- [x] `mvnw.cmd clean compile` – BUILD SUCCESS
- [x] Bộ kiểm thử hiện có đạt 64/64
- [x] Hoàn thành Unit Test B05
- [ ] Viết Unit Test riêng cho B02
- [ ] Kiểm thử Controller, CSRF và phân quyền
- [ ] Kiểm thử thao tác thực tế và dữ liệu MySQL
- [ ] Kiểm thử xử lý cập nhật đồng thời

## 6. Đường dẫn chức năng
- Quản lý đợt đăng ký: `RoundController`, `templates/rounds/`
- Danh sách đề tài: `/topics/my`
- Tạo đề xuất: `/topics/new`
- Chi tiết đề tài: `/topics/{id}`
- Chỉnh sửa đề tài: `/topics/{id}/edit`
- Gửi đề xuất: `POST /topics/{id}/submit`

## 7. Giới hạn hiện tại
- B02 đã hoàn thành phần code cơ bản nhưng chưa kiểm thử tích hợp đầy đủ.
- Chưa hoàn thiện kiểm thử phân quyền, CSRF và xử lý lỗi.
- B03 (duyệt và công bố đề tài), B04 và B06 chưa triển khai.
- B02 chưa được commit và push lên nhánh `Lam`.