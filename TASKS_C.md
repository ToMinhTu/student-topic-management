
<!-- Thanh dieu huong -->
| [📄 README](./README.md) | [📋 TASKS A](./TASKS_A.md) | [📋 TASKS B](./TASKS_B.md) | [📋 TASKS C](./TASKS_C.md) | [📋 TASKS D](./TASKS_D.md) |
| :--- | :--- | :--- | :--- | :--- |

# Phần C – Quản lý nhóm sinh viên và đăng ký đề tài

**Người thực hiện:** Khải

**Nhánh Git:** `Khai`

**Cập nhật:** 19/10/2026

**Pull Request:** [#18 – Hoàn thiện đăng ký và duyệt đề tài](https://github.com/ToMinhTu/student-topic-management/pull/18)

## 1. Quản lý nhóm sinh viên (C01)

- [x] C01.3 – Sinh viên tạo nhóm mới.
- [x] Tự động thiết lập người tạo nhóm làm trưởng nhóm.
- [x] Kiểm tra điều kiện tạo nhóm theo đợt đăng ký.
- [x] C01.4 – Sinh viên tham gia nhóm.
- [x] Kiểm tra số lượng thành viên tối đa.
- [x] Ngăn sinh viên tham gia nhiều nhóm đang hoạt động.
- [x] Hiển thị danh sách nhóm và chi tiết nhóm.

## 2. Quản lý thành viên nhóm (C02)

- [x] Thành viên rời nhóm.
- [x] Kiểm tra điều kiện rời nhóm.
- [x] Lưu lịch sử thành viên rời nhóm.
- [x] Chuyển quyền trưởng nhóm.
- [x] Chỉ trưởng nhóm được thực hiện chuyển quyền.
- [x] Chỉ chuyển quyền cho thành viên đang hoạt động.
- [x] Cập nhật vị trí trưởng nhóm và thứ tự thành viên.
- [x] Kiểm tra trạng thái nhóm và đợt đăng ký.

## 3. Giao diện quản lý nhóm

- [x] Trang danh sách nhóm sinh viên.
- [x] Trang tạo nhóm.
- [x] Trang chi tiết nhóm.
- [x] Chức năng tham gia nhóm.
- [x] Chức năng rời nhóm.
- [x] Chức năng chuyển quyền trưởng nhóm.
- [x] Thêm menu Nhóm sinh viên vào sidebar.

## 4. Kiểm thử C01–C02

- [x] Kiểm thử nghiệp vụ bằng JUnit 5 và Mockito.
- [x] Hoàn thành 16/16 bài kiểm thử `GroupServiceTest`.
- [x] Kiểm tra chức năng trên giao diện Spring Boot.
- [x] Kiểm tra kết quả thay đổi dữ liệu trong MySQL.

### Đường dẫn

- Danh sách nhóm: `GET /groups`
- Tạo nhóm: `GET /groups/create`
- Chi tiết nhóm: `GET /groups/{id}`

### Giới hạn

- Chưa hỗ trợ đầy đủ chuyển trưởng nhóm trong trường hợp cả 5 vị trí đều được sử dụng.
- Chưa triển khai hoàn chỉnh quy trình đóng nhóm và giải phóng thành viên sau khi kết thúc.

## 5. C03 – Đăng ký đề tài cho nhóm sinh viên

### C03.1 – Xem danh sách đề tài đã công bố

- [x] Hiển thị danh sách đề tài theo đợt đăng ký.
- [x] Chỉ hiển thị đề tài có trạng thái `PUBLISHED`.
- [x] Hiển thị mã đề tài, tên, bộ môn, mô tả và yêu cầu.
- [x] Hiển thị trạng thái còn trống hoặc đã được đăng ký.
- [x] Tích hợp giao diện với layout và sidebar chung.

### C03.2 – Trưởng nhóm đăng ký đề tài

- [x] Chỉ trưởng nhóm được phép đăng ký.
- [x] Kiểm tra trạng thái và thời hạn đợt đăng ký.
- [x] Kiểm tra số lượng thành viên trong nhóm.
- [x] Chỉ đăng ký đề tài đã công bố.
- [x] Ngăn một nhóm đăng ký nhiều lần.
- [x] Ngăn nhiều nhóm giữ chỗ cùng một đề tài.
- [x] Lưu hồ sơ vào bảng `topic_registrations`.
- [x] Thiết lập trạng thái ban đầu `PENDING`.
- [x] Hiển thị nút Đăng ký khi đủ điều kiện.

### C03.3 – Snapshot thành viên và audit log

- [x] Bổ sung bảng `registration_members` trong `schema_mysql.sql`.
- [x] Lưu danh sách thành viên tại thời điểm đăng ký.
- [x] Lưu vị trí `slot_no` của từng thành viên.
- [x] Thực hiện tạo hồ sơ và snapshot trong transaction.
- [x] Ghi audit log `REGISTRATION_SUBMITTED`.
- [x] Bổ sung kiểm thử cho snapshot và audit log.

### Kiểm thử C03

- [x] Kiểm thử đăng ký đề tài qua giao diện Spring Boot.
- [x] Xác nhận hồ sơ `PENDING` trong MySQL.
- [x] Kiểm tra dữ liệu bảng `registration_members`.
- [x] Hoàn thành 6/6 bài kiểm thử `TopicRegistrationServiceTest`.
- [ ] Kiểm thử đồng thời nhiều nhóm tranh cùng một đề tài trên MySQL thật.
- [ ] Hoàn thiện quy trình hủy và đăng ký lại.

### Đường dẫn C03

- Danh sách đề tài: `GET /topics?roundId={roundId}`
- Đăng ký đề tài: `POST /topics/{topicId}/register`

### Giới hạn C03

- Mỗi nhóm chỉ có một hồ sơ theo ràng buộc `UNIQUE(group_id)`.
- Chưa hỗ trợ đầy đủ đăng ký lại khi hồ sơ bị từ chối.
- Cần thống nhất migration bảng `registration_members` với thành viên phụ trách schema.

## 6. C04 – Trưởng khoa xét duyệt đăng ký đề tài

### C04.1 – Danh sách hồ sơ đăng ký

- [x] Xây dựng `DeanRegistrationController`.
- [x] Xây dựng `DeanRegistrationRepository`.
- [x] Hiển thị danh sách hồ sơ đăng ký.
- [x] Lọc hồ sơ theo đợt đăng ký.
- [x] Lọc theo trạng thái `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`.
- [x] Hỗ trợ phân trang.
- [x] Hiển thị mã hồ sơ, nhóm, đề tài, ngày gửi và trạng thái.
- [x] Kiểm tra quyền `DEAN`.
- [x] Tích hợp layout và sidebar chung.

### C04.2 – Duyệt hồ sơ đăng ký

- [x] Xây dựng `DeanRegistrationService`.
- [x] Xây dựng endpoint `POST /dean/registrations/{registrationId}/approve`.
- [x] Chỉ người dùng có quyền `DEAN` được duyệt.
- [x] Chỉ xử lý hồ sơ có trạng thái `PENDING`.
- [x] Kiểm tra nhóm ở trạng thái `FORMING`.
- [x] Kiểm tra đề tài đã `PUBLISHED`.
- [x] Kiểm tra nhóm và đề tài thuộc cùng đợt đăng ký.
- [x] Kiểm tra số lượng thành viên theo giới hạn đợt.
- [x] Kiểm tra trưởng nhóm và snapshot thành viên.
- [x] Xử lý duyệt trong transaction.
- [x] Chuyển hồ sơ sang `APPROVED`.
- [x] Chuyển nhóm sang `LOCKED`.
- [x] Lưu người duyệt và thời gian duyệt.
- [x] Ghi audit log `REGISTRATION_APPROVED`.
- [x] Tạo nút Duyệt và hộp thoại xác nhận.
- [x] Tích hợp CSRF cho form POST.
- [x] Ẩn nút Duyệt đối với hồ sơ không còn `PENDING`.

### Kiểm thử C04.2

- [x] Hoàn thành 12/12 Unit Test của `DeanRegistrationServiceTest`.
- [x] Hoàn thành 3/3 Integration Test của `DeanRegistrationIntegrationTest`.
- [x] Kiểm thử duyệt thành công trên MySQL.
- [x] Kiểm tra ngăn duyệt lại hồ sơ đã duyệt.
- [x] Kiểm tra rollback khi ghi audit log thất bại.
- [x] Kiểm thử thao tác duyệt qua giao diện Spring Boot.
- [x] Xác nhận hồ sơ chuyển sang `APPROVED`.
- [x] Xác nhận nhóm chuyển sang `LOCKED`.
- [x] Xác nhận audit log được ghi thành công.
- [x] Kiểm tra database C01 không bị thay đổi.

### Môi trường kiểm thử

**Ứng dụng chính:**

- Cổng: `8080`
- Database: `student_topic_management_c01`

**Ứng dụng kiểm thử C04:**

- Cổng: `8081`
- Profile: `c04test`
- Database: `student_topic_management_c04_test`

### C04.3 – Từ chối và xử lý đăng ký lại

- [ ] Trưởng khoa từ chối hồ sơ `PENDING`.
- [ ] Lưu lý do từ chối.
- [ ] Ghi audit log khi từ chối.
- [ ] Kiểm tra việc giải phóng đề tài đã giữ chỗ.
- [ ] Cho phép đăng ký lại theo quy tắc nghiệp vụ.
- [ ] Kiểm thử transaction và phân quyền.

### C04.4 – Hủy đăng ký

- [ ] Xây dựng chức năng hủy đăng ký.
- [ ] Kiểm tra trạng thái và quyền hủy.
- [ ] Ghi audit log khi hủy.
- [ ] Kiểm thử các trường hợp không được hủy.

## 7. C05 – Nộp và tải báo cáo

- [ ] Upload báo cáo PDF.
- [ ] Kiểm tra định dạng và dung lượng file.
- [ ] Lưu phiên bản báo cáo.
- [ ] Lưu thông tin file và SHA-256.
- [ ] Phân quyền tải báo cáo.
- [ ] Kiểm thử upload, download và quyền truy cập.

## 8. C06 – Kiểm thử tích hợp và bàn giao

- [x] Kiểm thử C03 bằng JUnit và Mockito.
- [x] Kiểm thử C04.2 bằng JUnit và Mockito.
- [x] Kiểm thử C04.2 trên MySQL thật.
- [x] Kiểm thử C04.2 qua giao diện.
- [x] Tạo commit `6f15a51`.
- [x] Push code lên nhánh `Khai`.
- [x] Tạo Pull Request #18.
- [ ] Hoàn tất review của trưởng nhóm.
- [ ] Merge Pull Request #18 vào `main`.
- [ ] Kiểm thử toàn bộ dự án sau merge.
- [ ] Kiểm thử tích hợp với phần A, B và D.
- [ ] Chuẩn bị dữ liệu demo và minh chứng báo cáo.

## 9. Git và Pull Request

- Repository: `ToMinhTu/student-topic-management`
- Nhánh làm việc: `Khai`
- Commit C03–C04.2: `6f15a51`
- Pull Request: https://github.com/ToMinhTu/student-topic-management/pull/18
- Trạng thái: Đã tạo PR, chờ review và merge.

### Lưu ý

Không commit các file kiểm thử cục bộ:

- `schema_c04_test_local.sql`
- `seed_dean_c01_local.sql`

## 10. Các công việc tiếp theo

- [ ] Chỉnh sửa `sidebar.html` theo cấu trúc mới nhất từ `main`, chỉ bổ sung menu phần C.
- [ ] Thống nhất migration `registration_members` với thành viên phụ trách database.
- [ ] Hoàn thiện C04.3 – Từ chối đăng ký.
- [ ] Hoàn thiện C04.4 – Hủy đăng ký.
- [ ] Hoàn thiện chức năng đăng ký lại theo yêu cầu.
- [ ] Triển khai C05 – Báo cáo PDF.
- [ ] Kiểm thử toàn bộ dự án và xử lý lỗi tích hợp.
- [ ] Chờ trưởng nhóm review và merge PR.
