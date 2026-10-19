Phần C – Quản lý nhóm sinh viên và đăng ký đề tài
Người thực hiện: Khải
Nhánh Git: Khai
Cập nhật: 20/10/2026
Pull Request: #18 – Phần C
Trạng thái: Đã hoàn thành C01, C02, C03 và C04.1–C04.3 (xử lý từ chối). Chưa hoàn thành đăng ký lại sau từ chối, C04.4, C05 và kiểm thử tích hợp toàn dự án. Các thay đổi C04.3 đang nằm ở máy cục bộ, chưa commit/push.

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
- [ ] Xây dựng chức năng hủy đăng ký.
- [ ] Kiểm tra trạng thái và quyền hủy.
- [ ] Ghi audit log khi hủy.
- [ ] Kiểm thử các trường hợp không được hủy.
7. C05 – Nộp và tải báo cáo
- [ ] Upload báo cáo PDF.
- [ ] Kiểm tra định dạng và dung lượng file.
- [ ] Lưu phiên bản báo cáo.
- [ ] Lưu thông tin file và SHA-256.
- [ ] Kiểm thử upload, download và quyền truy cập.
8. C06 – Kiểm thử tích hợp và bàn giao
- [x] Kiểm thử C03 bằng JUnit và Mockito.
- [x] Kiểm thử C04.2 bằng JUnit và Mockito.
- [x] Kiểm thử C04.2 trên MySQL thật.
- [x] Kiểm thử C04.2 qua giao diện.
- [x] Kiểm thử C04.3 bằng JUnit và Mockito.
- [x] Kiểm thử C04.3 trên MySQL thật.
- [x] Kiểm thử C04.3 qua giao diện Spring Boot.
- [x] Xác nhận rollback khi ghi audit từ chối thất bại.
- [x] Tạo commit 6f15a51 (C03–C04.2).
- [x] Push code lên nhánh Khai và tạo Pull Request #18.
- [ ] Commit và push thay đổi C04.3 đang nằm trên máy cục bộ.
- [ ] Hoàn tất review của trưởng nhóm.
- [ ] Merge Pull Request #18 vào main.
- [ ] Kiểm thử toàn bộ dự án sau merge.
- [ ] Kiểm thử tích hợp với phần A, B và D.
- [ ] Chuẩn bị dữ liệu demo và minh chứng báo cáo.
9. Git và Pull Request
- Repository: ToMinhTu/student-topic-management.
- Nhánh làm việc: Khai.
- Commit C03–C04.2: 6f15a51.
- Commit cập nhật sidebar/TASKS_C trước đó: ace49d3.
- Pull Request: https://github.com/ToMinhTu/student-topic-management/pull/18.
- Trạng thái: PR đã tạo; C04.3 chưa commit/push, chưa được xác nhận merge.
Lưu ý về file riêng trên máy
Không commit hoặc push các file:
- schema_c04_test_local.sql
- seed_dean_c01_local.sql
- sidebar_khai_backup_local.html
Không dùng git add . hoặc git add -A khi các file này còn nằm trong thư mục dự án.
10. Các công việc tiếp theo
- [x] Đồng bộ cách bố trí sidebar.html theo main, chỉ thêm menu thuộc phần C.
- [x] Hoàn thiện C04.3 – Xử lý từ chối đăng ký.
- [ ] Cập nhật TASKS_C.md, commit và push C04.3 lên nhánh Khai.
- [ ] Thống nhất migration registration_members với thành viên phụ trách database.
- [ ] Hoàn thiện đăng ký lại sau khi hồ sơ bị từ chối (lưu ý UNIQUE(group_id)).
- [ ] Hoàn thiện C04.4 – Hủy đăng ký.
- [ ] Triển khai C05 – Báo cáo PDF.
- [ ] Kiểm tra vấn đề tiếng Việt và múi giờ của dữ liệu từ chối.
- [ ] Kiểm thử tích hợp toàn dự án và xử lý lỗi phát sinh.
- [ ] Chờ trưởng nhóm review và merge PR #18.