Phần C – Quản lý nhóm sinh viên và đăng ký đề tài
Người thực hiện: Khải
Nhánh Git: Khai

1. Quản lý nhóm sinh viên (C01)
- [x] C01.3 – Sinh viên tạo nhóm mới.
- [x] Tự động thiết lập người tạo nhóm làm trưởng nhóm.
- [x] Kiểm tra điều kiện tạo nhóm theo đợt đăng ký.
- [x] C01.4 – Sinh viên tham gia nhóm.
- [x] Kiểm tra số lượng thành viên tối đa.
- [x] Ngăn sinh viên tham gia nhiều nhóm đang hoạt động.
- [x] Hiển thị danh sách nhóm và chi tiết nhóm.
2. Quản lý thành viên nhóm (C02)
- [x] Thành viên rời nhóm.
- [x] Kiểm tra điều kiện rời nhóm.
- [x] Lưu lịch sử thành viên rời nhóm.
- [x] Chuyển quyền trưởng nhóm.
- [x] Chỉ trưởng nhóm được thực hiện chuyển quyền.
- [x] Chỉ chuyển quyền cho thành viên đang hoạt động.
- [x] Cập nhật vị trí trưởng nhóm và thứ tự thành viên.
- [x] Kiểm tra trạng thái nhóm và đợt đăng ký.
3. Giao diện quản lý nhóm
- [x] Trang danh sách nhóm sinh viên.
- [x] Trang tạo nhóm.
- [x] Trang chi tiết nhóm.
- [x] Chức năng tham gia nhóm.
- [x] Chức năng rời nhóm.
- [x] Chức năng chuyển quyền trưởng nhóm.
- [x] Tích hợp menu Nhóm sinh viên với sidebar chung.
4. Kiểm thử C01–C02
- [x] Kiểm thử nghiệp vụ bằng JUnit 5 và Mockito.
- [x] Hoàn thành 16/16 bài kiểm thử GroupServiceTest.
- [x] Kiểm tra chức năng trên giao diện Spring Boot.
- [x] Kiểm tra dữ liệu thay đổi trong MySQL.
Đường dẫn
- Danh sách nhóm: GET /groups.
- Tạo nhóm: GET /groups/create.
- Chi tiết nhóm: GET /groups/{id}.
Giới hạn
- Chưa hỗ trợ đầy đủ chuyển trưởng nhóm khi cả 5 vị trí thành viên đều được sử dụng.
- Chưa hoàn thiện quy trình đóng nhóm và giải phóng thành viên khi kết thúc.
5. C03 – Đăng ký đề tài cho nhóm sinh viên
C03.1 – Xem danh sách đề tài đã công bố
- [x] Hiển thị danh sách đề tài theo đợt đăng ký.
- [x] Chỉ hiển thị đề tài có trạng thái PUBLISHED.
- [x] Hiển thị mã đề tài, tên, bộ môn, mô tả và yêu cầu.
- [x] Hiển thị trạng thái còn trống hoặc đã được đăng ký.
- [x] Tích hợp giao diện với layout và sidebar chung.
C03.2 – Trưởng nhóm đăng ký đề tài
- [x] Chỉ trưởng nhóm được phép đăng ký.
- [x] Kiểm tra trạng thái và thời hạn đợt đăng ký.
- [x] Kiểm tra nhóm đủ điều kiện số lượng thành viên.
- [x] Chỉ cho phép đăng ký đề tài đã công bố.
- [x] Ngăn một nhóm tạo nhiều hồ sơ đăng ký.
- [x] Ngăn hai nhóm giữ chỗ cùng một đề tài.
- [x] Lưu hồ sơ vào bảng topic_registrations với trạng thái PENDING.
- [x] Ghi snapshot thành viên vào registration_members.
- [x] Ghi audit REGISTRATION_SUBMITTED.
- [x] Hiển thị thao tác đăng ký cho trưởng nhóm đủ điều kiện.
Kiểm thử C03
- [x] Kiểm thử đăng ký qua giao diện Spring Boot.
- [x] Xác nhận hồ sơ PENDING trong MySQL.
- [x] Kiểm tra dữ liệu bảng registration_members.
- [x] Hoàn thành 6/6 bài kiểm thử TopicRegistrationServiceTest.
- [ ] Kiểm thử đồng thời nhiều nhóm tranh cùng một đề tài trên MySQL thật.
- [ ] Hoàn thiện quy trình hủy và đăng ký lại.
Đường dẫn C03
- Danh sách đề tài: GET /topics?roundId={roundId}.
- Đăng ký đề tài: POST /topics/{topicId}/register.
Giới hạn C03
- Mỗi nhóm chỉ có một hồ sơ theo ràng buộc UNIQUE(group_id).
- Chưa hỗ trợ đầy đủ đăng ký lại khi hồ sơ bị từ chối.
- Cần thống nhất migration bảng registration_members với thành viên phụ trách schema.
6. C04 – Trưởng khoa xét duyệt đăng ký đề tài
C04.1 – Danh sách hồ sơ đăng ký
- [x] Xây dựng DeanRegistrationController.
- [x] Xây dựng DeanRegistrationRepository.
- [x] Hiển thị danh sách hồ sơ đăng ký.
- [x] Lọc hồ sơ theo đợt đăng ký.
- [x] Lọc theo trạng thái PENDING, APPROVED, REJECTED, CANCELLED.
- [x] Hỗ trợ phân trang.
- [x] Hiển thị mã hồ sơ, nhóm, đề tài, ngày gửi và trạng thái.
- [x] Kiểm tra quyền DEAN.
- [x] Tích hợp layout và sidebar chung.
C04.2 – Duyệt hồ sơ đăng ký
- [x] Xây dựng DeanRegistrationService.
- [x] Xây dựng endpoint POST /dean/registrations/{registrationId}/approve.
- [x] Chỉ người dùng có quyền DEAN được duyệt.
- [x] Chỉ xử lý hồ sơ có trạng thái PENDING.
- [x] Kiểm tra nhóm ở trạng thái FORMING.
- [x] Kiểm tra đề tài đã PUBLISHED.
- [x] Kiểm tra nhóm và đề tài thuộc cùng đợt đăng ký.
- [x] Kiểm tra số lượng thành viên theo giới hạn đợt.
- [x] Kiểm tra trưởng nhóm và snapshot thành viên.
- [x] Xử lý duyệt trong transaction.
- [x] Chuyển hồ sơ sang APPROVED.
- [x] Chuyển nhóm sang LOCKED.
- [x] Lưu người duyệt và thời gian duyệt.
- [x] Ghi audit log REGISTRATION_APPROVED.
- [x] Tạo nút Duyệt và hộp thoại xác nhận.
- [x] Tích hợp CSRF cho form POST.
- [x] Ẩn nút Duyệt đối với hồ sơ không còn PENDING.
Kiểm thử C04.2
- [x] Hoàn thành 12/12 Unit Test của DeanRegistrationServiceTest.
- [x] Hoàn thành 3/3 Integration Test của DeanRegistrationIntegrationTest.
- [x] Kiểm thử duyệt thành công trên MySQL.
- [x] Kiểm tra ngăn duyệt lại hồ sơ đã duyệt.
- [x] Kiểm tra rollback khi ghi audit log thất bại.
- [x] Kiểm thử thao tác duyệt qua giao diện Spring Boot.
- [x] Xác nhận hồ sơ chuyển sang APPROVED.
- [x] Xác nhận nhóm chuyển sang LOCKED.
- [x] Xác nhận audit log được ghi thành công.
Môi trường kiểm thử
Ứng dụng chính:
- Cổng: 8080.
- Database: student_topic_management_c01.
Ứng dụng kiểm thử C04:
- Cổng: 8081.
- Profile: c04test.
- Database: student_topic_management_c04_test.
- Lưu ý: application-c04test.yml nằm trong src/test/resources; khi chạy bằng spring-boot:run cần chỉ định rõ spring.datasource.url cho database C04 và xác nhận lại log Database JDBC URL.
C04.3 – Từ chối và xử lý đăng ký lại
- [x] Trưởng khoa từ chối hồ sơ đang PENDING.
- [x] Tạo endpoint POST /dean/registrations/{registrationId}/reject.
- [x] Kiểm tra quyền DEAN.
- [x] Bắt buộc lý do từ chối, sau khi loại khoảng trắng thừa và giới hạn tối đa 1000 ký tự.
- [x] Lưu lý do vào decision_note.
- [x] Lưu người quyết định vào decided_by.
- [x] Lưu thời gian quyết định vào decided_at.
- [x] Chuyển trạng thái hồ sơ PENDING → REJECTED.
- [x] Giải phóng đề tài đã giữ chỗ (reserved_topic_id = NULL, do cột sinh tự động).
- [x] Giữ nhóm ở trạng thái FORMING.
- [x] Ghi audit log REGISTRATION_REJECTED.
- [x] Thực hiện từ chối trong transaction; rollback nếu ghi audit lỗi.
- [x] Ngăn từ chối hồ sơ lần thứ hai.
- [x] Thêm nút Từ chối và modal nhập lý do trong templates/dean/registrations/list.html.
- [x] Sử dụng CSRF của Spring Security / Thymeleaf trong form POST.
- [x] Chỉ hiển thị nút Duyệt và Từ chối với hồ sơ PENDING.
- [x] Hoàn thành 5 Unit Test C04.3.
- [x] Hoàn thành 3 Integration Test C04.3.
- [x] Kiểm thử từ chối thành công qua trình duyệt trên cổng 8081.
- [x] Xác nhận MySQL: hồ sơ REJECTED, nhóm FORMING, reserved_topic_id = NULL, audit REGISTRATION_REJECTED.
- [ ] Cho phép đăng ký lại sau khi bị từ chối theo quy tắc nghiệp vụ.
Tổng kết kiểm thử C04.2–C04.3
- Unit Test: 17/17 PASS (12 C04.2, 5 C04.3).
- Integration Test: 6/6 PASS (3 C04.2, 3 C04.3).
- Tổng: 23/23 PASS.
- Đã thử duyệt và từ chối qua giao diện với dữ liệu kiểm thử C04; không chạy những thao tác này trên database C01.
Vấn đề cần rà soát
- Ký tự tiếng Việt trong decision_note hiện được MySQL Terminal hiển thị thành dấu ?; cần xác định do client encoding hay dữ liệu đã bị lưu sai.
- decided_at và audit_logs.created_at lệch khoảng 7 giờ; cần thống nhất quy ước múi giờ UTC/Asia/Ho_Chi_Minh.
- Sidebar hiển thị nhãn vai trò “Sinh viên” với tài khoản dean.c04.test; cần kiểm tra logic trình bày vai trò, dù chức năng DEAN đã truy cập được.
C04.4 – Hủy đăng ký
- [x] Xây dựng chức năng hủy đăng ký đề tài.
- [x] Chỉ trưởng nhóm được phép hủy.
- [x] Chỉ cho phép hủy hồ sơ PENDING.
- [x] Kiểm tra nhóm FORMING và thời hạn đăng ký.
- [x] Chuyển trạng thái PENDING → CANCELLED.
- [x] Giải phóng đề tài khi hủy (reserved_topic_id = NULL).
- [x] Ghi audit REGISTRATION_CANCELLED.
- [x] Giữ nguyên hồ sơ và snapshot thành viên.
- [x] Cho phép đăng ký lại sau hủy (CANCELLED → PENDING).
- [x] Unit Test: 21/21 PASS (toàn bộ TopicRegistrationServiceTest).
- [x] Integration Test C04.4: 3/3 PASS.
- [x] Kiểm thử hủy đăng ký qua giao diện.
- [x] Kiểm tra trạng thái CANCELLED và audit trên MySQL.
- [ ] Xác nhận lần cuối luồng đăng ký lại sau hủy trên giao diện.
## 7. C05 – Nộp và tải báo cáo PDF

### C05.1 – Nộp báo cáo PDF
- [x] Xây dựng `ReportController.java`.
- [x] Xây dựng `ReportService.java`.
- [x] Xây dựng `ReportRepository.java`.
- [x] Cho phép trưởng nhóm nộp báo cáo PDF.
- [x] Chỉ cho phép nộp khi hồ sơ đăng ký ở trạng thái `APPROVED`.
- [x] Kiểm tra nhóm đã ở trạng thái `LOCKED`.
- [x] Kiểm tra quyền trưởng nhóm trước khi lưu báo cáo.
- [x] Kiểm tra hạn nộp báo cáo khi đã được thiết lập.
- [x] Kiểm tra phần mở rộng `.pdf` và dấu hiệu định dạng PDF.
- [x] Giới hạn dung lượng file tối đa 10 MB.
- [x] Lưu file vào thư mục `uploads/reports/`.
- [x] Không đưa thư mục lưu PDF lên GitHub.

### C05.2 – Quản lý phiên bản báo cáo
- [x] Lưu thông tin báo cáo vào bảng `reports`.
- [x] Tự động tăng số phiên bản khi nộp báo cáo mới.
- [x] Lưu tên file gốc, dung lượng và thời gian nộp.
- [x] Lưu thông tin người nộp.
- [x] Tạo khóa lưu trữ ngẫu nhiên bằng UUID.
- [x] Tính và lưu mã SHA-256.
- [x] Sử dụng transaction và khóa hồ sơ đăng ký để hạn chế trùng phiên bản.

### C05.3 – Xem và tải báo cáo PDF
- [x] Hiển thị danh sách lịch sử các phiên bản báo cáo.
- [x] Cho phép thành viên đang hoạt động trong nhóm xem danh sách báo cáo.
- [x] Cho phép thành viên tải báo cáo PDF.
- [x] Kiểm tra quyền truy cập trước khi tải file.
- [x] Kiểm tra dung lượng và SHA-256 khi tải file.
- [x] Chỉ trưởng nhóm nhìn thấy biểu mẫu nộp báo cáo.
- [x] Hiển thị thông báo khi thành viên không có quyền nộp báo cáo.

### C05.4 – Kết quả kiểm thử
- [x] Trưởng nhóm `student.c04.01` nộp PDF thành công.
- [x] Nộp thành công 2 phiên bản báo cáo.
- [x] Kiểm tra dữ liệu báo cáo trong MySQL.
- [x] Thành viên `student.c04.02` xem được danh sách báo cáo.
- [x] Thành viên tải và mở được PDF phiên bản 2.
- [x] Thành viên không được phép nộp PDF.
- [x] Hệ thống hiển thị thông báo từ chối quyền.
- [x] Giao diện ẩn biểu mẫu upload đối với thành viên.
- [x] Biên dịch Maven thành công (`BUILD SUCCESS`).
- [ ] Bổ sung kiểm thử tự động cho `ReportService`.
- [ ] Kiểm thử đầy đủ file vượt quá 10 MB.
- [ ] Kiểm thử người ngoài nhóm truy cập và tải báo cáo.
- [ ] Kiểm thử nộp báo cáo sau thời hạn.
- [ ] Kiểm thử tích hợp C05 sau khi merge vào `main`.

### Đường dẫn C05
- Danh sách báo cáo: `GET /reports/{registrationId}`.
- Nộp báo cáo: `POST /reports/{registrationId}/upload`.
- Tải báo cáo: `GET /reports/files/{reportId}/download`.

### Môi trường kiểm thử C05
- Spring Boot 4.1.1.
- Cổng: `8081`.
- Profile: `c04test`.
- Database: `student_topic_management_c04_test`.

### Hạn chế cần hoàn thiện
- Chưa tích hợp đầy đủ đường dẫn báo cáo vào menu hoặc trang nhóm.
- Cần thống nhất cách hiển thị thời gian theo múi giờ Việt Nam.
- Cần kiểm thử thêm các trường hợp lỗi và giới hạn upload.

---

## 8. C06 – Kiểm thử tích hợp và bàn giao

### Các công việc đã hoàn thành
- [x] Kiểm thử C01–C02 bằng JUnit và Mockito.
- [x] Hoàn thành 16/16 bài kiểm thử `GroupServiceTest`.
- [x] Kiểm thử C03 bằng JUnit và Mockito.
- [x] Hoàn thành 6/6 bài kiểm thử C03 ở giai đoạn kiểm thử ban đầu.
- [x] Kiểm thử C04.2 bằng Unit Test và Integration Test.
- [x] Kiểm thử C04.3 bằng Unit Test và Integration Test.
- [x] Kiểm thử C04.4 bằng Unit Test và Integration Test.
- [x] Kiểm thử duyệt, từ chối và hủy đăng ký qua giao diện.
- [x] Kiểm tra dữ liệu C04 trên MySQL thử nghiệm.
- [x] Kiểm thử thủ công chức năng upload và download C05.
- [x] Kiểm thử phân quyền trưởng nhóm và thành viên trong C05.
- [x] Commit và push code C05 lên nhánh `Khai`.
- [x] Cập nhật tiêu đề và mô tả Pull Request #22.

### Công việc chưa hoàn thành
- [ ] Hoàn tất review của trưởng nhóm.
- [ ] Merge Pull Request #22 vào `main`.
- [ ] Chạy lại toàn bộ bộ kiểm thử trên code mới nhất.
- [ ] Kiểm thử tích hợp C01–C05 sau merge.
- [ ] Kiểm thử tích hợp với phần A, B và D.
- [ ] Kiểm tra phân quyền giữa các vai trò trong toàn hệ thống.
- [ ] Chuẩn bị dữ liệu demo và hình ảnh minh chứng.
- [ ] Hoàn thiện báo cáo và bàn giao chức năng phần C.

---

## 9. Git và Pull Request

### Thông tin repository
- Repository: `ToMinhTu/student-topic-management`.
- Nhánh làm việc: `Khai`.
- Nhánh đích: `main`.

### Các commit quan trọng
- C03–C04.2: `6f15a51`.
- Cập nhật sidebar/TASKS_C trước đó: `ace49d3`.
- C05 – PDF Report Upload & Download: `765b630`.

### Trạng thái hiện tại
- [x] Code C05 đã commit trên nhánh `Khai`.
- [x] Code C05 đã push lên GitHub.
- [x] Cập nhật tiêu đề PR #22 bao gồm C02–C03–C05.
- [x] Cập nhật mô tả các chức năng và kết quả kiểm thử.
- [x] Xóa nhánh thử nghiệm `feature/khai-c05`.
- [ ] Hoàn tất review PR #22.
- [ ] Xác nhận PR #22 đã merge vào `main`.

**Lưu ý:** Cần kiểm tra trạng thái PR #18 và các commit đã được đưa vào `main` trước khi thực hiện merge tiếp theo.

### Các file riêng trên máy

Không commit hoặc push các file:

- `TopicRegistrationController_backup_local.java`
- `TopicRegistrationService_backup_local.java`
- `schema_c04_test_local.sql`
- `seed_dean_c01_local.sql`
- `sidebar_khai_backup_local.html`
- `topics_list_backup_local.html`

Không dùng `git add .`, `git add -A` hoặc `git clean -fd` khi chưa xác định rõ các file cần xử lý.

Không commit dữ liệu PDF trong `uploads/reports/`.

---

## 10. Các công việc tiếp theo

### Hoàn thiện chức năng
- [x] Hoàn thiện C01 – Quản lý nhóm sinh viên.
- [x] Hoàn thiện C02 – Quản lý thành viên và trưởng nhóm.
- [x] Triển khai C03 – Đăng ký đề tài.
- [x] Hoàn thiện C04.1 – Danh sách hồ sơ.
- [x] Hoàn thiện C04.2 – Duyệt đăng ký.
- [x] Hoàn thiện C04.3 – Từ chối đăng ký.
- [x] Triển khai C04.4 – Hủy đăng ký.
- [x] Triển khai C05 – Nộp và tải báo cáo PDF.

### Công việc cần tiếp tục
- [ ] Rà soát lần cuối quy trình đăng ký lại sau từ chối hoặc hủy.
- [ ] Hoàn thiện tích hợp C05 vào giao diện chính.
- [ ] Kiểm thử giới hạn dung lượng và hạn nộp PDF.
- [ ] Kiểm thử quyền truy cập của người ngoài nhóm.
- [ ] Thống nhất migration `registration_members` với thành viên phụ trách database.
- [ ] Kiểm tra vấn đề hiển thị tiếng Việt và múi giờ.
- [ ] Bổ sung kiểm thử tự động cho C05.
- [ ] Chờ trưởng nhóm review và merge PR #22.
- [ ] Đồng bộ code với `main` sau khi merge.
- [ ] Kiểm thử tích hợp với phần A, B và D.
- [ ] Hoàn thiện dữ liệu demo, tài liệu và báo cáo đồ án.

**Tổng kết:** Các chức năng C01–C05 đã được triển khai. C05 đã hoàn thành các kiểm thử thủ công cơ bản và được push lên GitHub. Phần C chưa hoàn tất bàn giao vì vẫn còn kiểm thử bổ sung, tích hợp toàn hệ thống và review/merge Pull Request.