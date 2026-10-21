# Đối chiếu student-topic-management-Toan và student-topic-management-main

Ngày kiểm tra: 22/10/2026 (Asia/Saigon).

## Phạm vi và kết quả

- A: `D:/test/student-topic-management-Toan` (bản hiện tại sau lần gộp trước).
- B: `D:/test/student-topic-management-main`.
- Đã xác nhận 259 file của ZIP gốc có mặt và trùng SHA-256 với file tương ứng trong thư mục B.
- So sánh SHA-256 toàn bộ file thuộc phạm vi: 154 file giống nhau, 104 file cùng đường dẫn nhưng khác nội dung, 22 file chỉ có ở Toan, 0 file chỉ có ở main.
- Loại trừ `target/`, `.git/`, `.github/`, `.m2repo/`, `.env`, `debug.log`. Báo cáo này được tạo sau thời điểm thống kê.
- Kiểm kê file là đầy đủ trong phạm vi trên; rà soát logic tập trung vào các điểm giao nhau của hai bản và lỗi từ kiểm thử, không phải chứng nhận toàn bộ ứng dụng không còn lỗi.
- Nội dung đính kèm là diffstat (số dòng thêm/xóa), không đủ để lựa chọn phiên bản tốt hơn. Ví dụ `StudentTopicManagementApplication.java` hiện giống nhau giữa hai thư mục, dù xuất hiện trong diffstat bạn gửi.

## Các vấn đề cần sửa trước khi coi bản gộp hoàn chỉnh

### 1. Cao: luồng duyệt/công bố cũ bỏ qua quy trình main

File: `src/main/java/com/example/student_topic_management/service/TopicProposalService.java:361`, `:378`, `:409`; controller tương ứng là `controller/TopicProposalController.java`.

Toan có thêm `approveTopic`, `rejectTopic`, `publishTopic`, cho phép LECTURER duyệt hoặc công bố đề tài theo ID. Nhánh duyệt chỉ kiểm tra SUBMITTED, không kiểm tra bộ môn đã đồng ý, cửa sổ đề xuất còn mở hay số giảng viên hướng dẫn. Nhánh công bố không kiểm tra chủ sở hữu và cho phép LECTURER.

Main có `DeanTopicReviewService` và `TopicPublicationService` yêu cầu DEAN; nhánh duyệt kiểm tra các điều kiện nghiệp vụ trên. Hai luồng cùng tồn tại làm mất hiệu lực quy trình chặt chẽ của main. Nên thống nhất quyền và chuyển các endpoint cũ sang luồng được kiểm soát tương ứng.

Kết luận từ rà soát mã, chưa thực hiện thao tác duyệt/công bố trên database.

### 2. Cao: xem được đề tài người khác qua ID

File: `src/main/java/com/example/student_topic_management/service/TopicProposalService.java:425`.

`getMyTopicDetail` của Toan dùng `findById(topicId)`, chỉ kiểm tra vai trò người gọi. Main dùng `findByIdAndProposedBy(topicId, user.getId())`. Vì vậy người có vai trò được phép có thể đọc bản nháp/chi tiết đề tài của người khác qua `/topics/{id}`; form sửa cũng dùng phương thức này. Các thao tác lưu có kiểm tra riêng, nên không suy diễn thành quyền sửa mọi đề tài.

Nên khôi phục kiểm tra chủ sở hữu của main. Test `queriesOnlyCurrentUsersTopics` hiện lỗi ở phần đọc chi tiết; phần danh sách đã dùng truy vấn theo chủ sở hữu.

### 3. Cao: lọc danh mục đề tài sai tham số

File: `src/main/java/com/example/student_topic_management/repository/TopicRepository.java:53`.

Service truyền `keywordPattern, roundId, departmentId`, nhưng repository Toan nhận `keyword, departmentId, supervisorId`. Hậu quả:
- roundId bị áp vào departmentId;
- departmentId thực tế bị bỏ qua vì `(:supervisorId IS NULL OR :supervisorId IS NOT NULL)` luôn đúng;
- bỏ tìm theo mã đề tài;
- không xử lý ESCAPE '!' tương ứng với mẫu từ khóa do service tạo;
- thiếu thứ tự ổn định và countQuery đã có ở main.

Đây là lỗi được đưa vào trong lần chỉnh repository trước; nên lấy truy vấn đầy đủ từ main thay cho truy vấn tạm này. Hai test khai báo truy vấn B05 hiện thất bại. Các test này kiểm tra nội dung khai báo, không chạy SQL thật.

### 4. Vừa: danh sách chờ trưởng khoa duyệt chứa hồ sơ chưa đủ điều kiện

File: `src/main/java/com/example/student_topic_management/repository/TopicRepository.java:58`.

Toan chỉ lọc SUBMITTED. Main còn lọc `departmentDecision = 'APPROVED'` và các trường quyết định/công bố chưa được đặt. Danh sách Toan có thể chứa đề tài chưa được bộ môn thông qua; thao tác duyệt chính thức vẫn có kiểm tra bổ sung trong service.

Nên khôi phục truy vấn main. Test `queuePaginatesAndDeclaresOnlyEligibleRows` hiện thất bại.

### 5. Vừa: trang hồ sơ ghép từ main thiếu tải quan hệ department

File: `identity/repository/StudentProfileRepository.java`, `identity/repository/LecturerProfileRepository.java`, `identity/controller/UserProfileController.java`, `templates/users/profile.html:86` và `:102`.

Controller/template hồ sơ đã lấy từ main, nhưng hai repository còn là bản Toan, không có `@EntityGraph(attributePaths = {"department"})` cho `findById`. Entity khai báo department LAZY, cấu hình `open-in-view: false`, template đọc `department.name` sau khi repository trả kết quả. Đây là đường dẫn có nguy cơ LazyInitializationException/500 khi mở hồ sơ sinh viên hoặc giảng viên.

Nên bổ sung EntityGraph từ main hoặc tải DTO đầy đủ trong transaction. Nhận định từ mã nguồn, chưa kiểm thử render trang này.

### 6. Vừa: service thông báo chưa hợp nhất đầy đủ

File: `identity/service/AnnouncementService.java:40`, `:41`, `:90`.

Các wrapper `createDraft` và `updateDraft` không có transaction/phân quyền ở điểm vào và gọi nội bộ `saveDraft`; lời gọi nội bộ không đi qua Spring proxy để áp dụng annotation của `saveDraft`. Đường HTTP quản lý hiện có chặn ADMIN/DEAN ở SecurityConfig, nên không kết luận người dùng thường có thể gọi trực tiếp URL này.

Ngoài ra `publish` không ghi AuditLog như bản main. Nên hợp nhất service thực sự, giữ kiểm soát tại entry point và ghi audit trong cùng transaction.

### 7. Vừa: menu và URL chưa đồng bộ hai bản

- Sidebar Toan không có các mục `/dean/topics/supervisors` và `/dean/registrations` đã có ở main, dù controller/template tương ứng hiện tồn tại.
- Đăng ký đề tài đổi từ `/topics` ở main sang `/topic-registration` ở Toan. Test B05 `khaiTopicsEndpointStillUsesItsOriginalControllerAndTemplate` vẫn yêu cầu URL/view cũ.
- SecurityConfig Toan giới hạn POST tạo đề tài cho LECTURER, trong khi service và test của main hỗ trợ STUDENT đề xuất. Cần thống nhất yêu cầu quyền trước khi chốt luồng này.

Nên giữ các menu riêng hữu ích của Toan, bổ sung các menu main, chọn URL chính thức và xử lý liên kết/test hoặc chuyển hướng tương thích.

### 8. Thấp: các thành phần cùng chức năng vẫn bị nhân đôi

Hai seeder admin, hai UserDetailsService, hai bean Clock và hai controller quản lý thông báo vẫn tồn tại. Qualifier chọn UserDetailsService và profile `legacy-announcements` giúp cấu hình mặc định tránh một số xung đột, nhưng chưa thống nhất thiết kế. Nếu bật profile legacy trong trạng thái hiện tại, cả hai controller thông báo cùng hoạt động và có mapping trùng.

Nên hợp nhất thành một implementation cho mỗi chức năng; giữ API tương thích nếu còn nơi gọi.

## Phần Toan cần bảo toàn khi gộp tiếp

- `assessment/policy/GradePolicy.java`, `ResultRequirementPolicy.java` và các kiểm thử tương ứng.
- `assessment/service/DefenseService.java`: kiểm tra hồ sơ đã duyệt/đúng đợt, lịch hội đồng trùng giờ, phân công phản biện và thao tác kết thúc bảo vệ bởi chủ tịch.
- `assessment/service/ResultService.java`: kiểm tra điều kiện tổng hợp kết quả, phiếu chấm, quyền chủ tịch và công bố.
- Luồng `LecturerRegistrationController`, các giao diện đăng ký riêng và những thay đổi giao diện của Toan.

Các điểm này là phần mở rộng cụ thể quan sát được; không có cơ sở để ghi đè toàn bộ Toan bằng main hoặc ngược lại.

## Kiểm thử thực hiện trong lần kiểm tra này

Chạy offline với dependency đã có, chỉ chọn unit test không kết nối database:

```powershell
mvn -o '-Dmaven.repo.local=C:\Users\Ton\.m2\repository' '-Dtest=PublishedTopicRepositoryContractB05Test,DeanTopicReviewServiceB032Test,TopicProposalServiceTest' test -q
```

| Nhóm test | Số ca | Failures | Errors |
| --- | ---: | ---: | ---: |
| PublishedTopicRepositoryContractB05Test | 2 | 2 | 0 |
| DeanTopicReviewServiceB032Test | 43 | 1 | 0 |
| TopicProposalServiceTest | 36 | 0 | 1 |
| Tổng | 81 | 3 | 1 |

77 ca đạt. Chưa chạy lại toàn bộ integration test; cấu hình dự án trỏ đến database bên ngoài. Các báo cáo cũ không được coi là kết quả mới của toàn bộ suite.

Kết luận trước đây rằng bản gộp đã hoàn chỉnh là chưa đủ căn cứ. Compile thành công và một test render hội đồng đạt không xác nhận các luồng còn lại đúng. Lần này chỉ kiểm tra và tạo báo cáo, không sửa mã nghiệp vụ hay database.

## Danh sách 104 file cùng đường dẫn nhưng khác nội dung

- `.gitignore`
- `README.md`
- `src/main/java/com/example/student_topic_management/assessment/controller/CouncilController.java`
- `src/main/java/com/example/student_topic_management/assessment/controller/DefenseController.java`
- `src/main/java/com/example/student_topic_management/assessment/controller/EvaluationController.java`
- `src/main/java/com/example/student_topic_management/assessment/controller/ResultController.java`
- `src/main/java/com/example/student_topic_management/assessment/dto/DefenseForm.java`
- `src/main/java/com/example/student_topic_management/assessment/dto/EvaluationForm.java`
- `src/main/java/com/example/student_topic_management/assessment/dto/ResultForm.java`
- `src/main/java/com/example/student_topic_management/assessment/repository/ResultRepository.java`
- `src/main/java/com/example/student_topic_management/assessment/service/CouncilService.java`
- `src/main/java/com/example/student_topic_management/assessment/service/DefenseService.java`
- `src/main/java/com/example/student_topic_management/assessment/service/EvaluationService.java`
- `src/main/java/com/example/student_topic_management/assessment/service/ResultService.java`
- `src/main/java/com/example/student_topic_management/audit/controller/AdminAuditLogController.java`
- `src/main/java/com/example/student_topic_management/audit/entity/AuditLog.java`
- `src/main/java/com/example/student_topic_management/audit/repository/AuditLogRepository.java`
- `src/main/java/com/example/student_topic_management/audit/service/AuditLogService.java`
- `src/main/java/com/example/student_topic_management/common/config/SecurityConfig.java`
- `src/main/java/com/example/student_topic_management/controller/RoundController.java`
- `src/main/java/com/example/student_topic_management/controller/TopicProposalController.java`
- `src/main/java/com/example/student_topic_management/identity/controller/LecturerController.java`
- `src/main/java/com/example/student_topic_management/identity/controller/PublicAnnouncementController.java`
- `src/main/java/com/example/student_topic_management/identity/controller/StudentController.java`
- `src/main/java/com/example/student_topic_management/identity/controller/UserManagementController.java`
- `src/main/java/com/example/student_topic_management/identity/dto/AnnouncementForm.java`
- `src/main/java/com/example/student_topic_management/identity/dto/ChangePasswordForm.java`
- `src/main/java/com/example/student_topic_management/identity/dto/UserListDTO.java`
- `src/main/java/com/example/student_topic_management/identity/entity/Announcement.java`
- `src/main/java/com/example/student_topic_management/identity/repository/AnnouncementRepository.java`
- `src/main/java/com/example/student_topic_management/identity/repository/LecturerProfileRepository.java`
- `src/main/java/com/example/student_topic_management/identity/repository/StudentProfileRepository.java`
- `src/main/java/com/example/student_topic_management/identity/service/AnnouncementService.java`
- `src/main/java/com/example/student_topic_management/identity/service/CurrentUserService.java`
- `src/main/java/com/example/student_topic_management/identity/service/SessionRevocationListener.java`
- `src/main/java/com/example/student_topic_management/identity/service/UserManagementService.java`
- `src/main/java/com/example/student_topic_management/notification/controller/AdminMailJobController.java`
- `src/main/java/com/example/student_topic_management/notification/dto/MailJob.java`
- `src/main/java/com/example/student_topic_management/notification/entity/EmailOutbox.java`
- `src/main/java/com/example/student_topic_management/notification/gateway/FakeMailGateway.java`
- `src/main/java/com/example/student_topic_management/notification/gateway/SmtpMailGateway.java`
- `src/main/java/com/example/student_topic_management/notification/repository/EmailOutboxRepository.java`
- `src/main/java/com/example/student_topic_management/notification/service/EmailOutboxService.java`
- `src/main/java/com/example/student_topic_management/notification/worker/MailOutboxWorker.java`
- `src/main/java/com/example/student_topic_management/registration/controller/DeanRegistrationController.java`
- `src/main/java/com/example/student_topic_management/registration/controller/GroupController.java`
- `src/main/java/com/example/student_topic_management/registration/controller/TopicRegistrationController.java`
- `src/main/java/com/example/student_topic_management/registration/dto/TopicView.java`
- `src/main/java/com/example/student_topic_management/registration/repository/DeanRegistrationRepository.java`
- `src/main/java/com/example/student_topic_management/registration/repository/TopicRepository.java`
- `src/main/java/com/example/student_topic_management/registration/service/DeanRegistrationService.java`
- `src/main/java/com/example/student_topic_management/registration/service/RoundAccessImpl.java`
- `src/main/java/com/example/student_topic_management/registration/service/TopicRegistrationService.java`
- `src/main/java/com/example/student_topic_management/repository/TopicRepository.java`
- `src/main/java/com/example/student_topic_management/service/TopicProposalService.java`
- `src/main/resources/application.yml`
- `src/main/resources/application-mail.yml`
- `src/main/resources/application-prod.yml`
- `src/main/resources/db/schema_c01_test.sql`
- `src/main/resources/db/schema_mysql.sql`
- `src/main/resources/templates/access-denied.html`
- `src/main/resources/templates/account/change-password.html`
- `src/main/resources/templates/announcements/detail.html`
- `src/main/resources/templates/audit-logs/list.html`
- `src/main/resources/templates/councils/form.html`
- `src/main/resources/templates/councils/list.html`
- `src/main/resources/templates/councils/member-form.html`
- `src/main/resources/templates/councils/members.html`
- `src/main/resources/templates/dean/registrations/list.html`
- `src/main/resources/templates/defenses/detail.html`
- `src/main/resources/templates/defenses/form.html`
- `src/main/resources/templates/defenses/list.html`
- `src/main/resources/templates/departments/form.html`
- `src/main/resources/templates/departments/list.html`
- `src/main/resources/templates/evaluations/detail.html`
- `src/main/resources/templates/evaluations/form.html`
- `src/main/resources/templates/evaluations/list.html`
- `src/main/resources/templates/fragments/header.html`
- `src/main/resources/templates/fragments/messages.html`
- `src/main/resources/templates/fragments/sidebar.html`
- `src/main/resources/templates/groups/create.html`
- `src/main/resources/templates/groups/detail.html`
- `src/main/resources/templates/groups/list.html`
- `src/main/resources/templates/home.html`
- `src/main/resources/templates/layout.html`
- `src/main/resources/templates/login.html`
- `src/main/resources/templates/mail-jobs/list.html`
- `src/main/resources/templates/management/announcements/form.html`
- `src/main/resources/templates/management/announcements/list.html`
- `src/main/resources/templates/results/detail.html`
- `src/main/resources/templates/results/form.html`
- `src/main/resources/templates/results/list.html`
- `src/main/resources/templates/rounds/detail.html`
- `src/main/resources/templates/rounds/form.html`
- `src/main/resources/templates/rounds/list.html`
- `src/main/resources/templates/topics/detail.html`
- `src/main/resources/templates/topics/form.html`
- `src/main/resources/templates/topics/list.html`
- `src/main/resources/templates/topics/my-list.html`
- `src/main/resources/templates/users/list.html`
- `src/test/java/com/example/student_topic_management/controller/RoundControllerTest.java`
- `TASKS_A.md`
- `TASKS_B.md`
- `TASKS_C.md`

## Danh sách 22 file chỉ có ở Toan

- `src/main/java/com/example/student_topic_management/assessment/policy/GradePolicy.java`
- `src/main/java/com/example/student_topic_management/assessment/policy/ResultRequirementPolicy.java`
- `src/main/java/com/example/student_topic_management/identity/controller/AccountController.java`
- `src/main/java/com/example/student_topic_management/identity/controller/AnnouncementManagementController.java`
- `src/main/java/com/example/student_topic_management/identity/service/AccountSecurityService.java`
- `src/main/java/com/example/student_topic_management/identity/service/AdminSeeder.java`
- `src/main/java/com/example/student_topic_management/identity/service/DatabaseUserDetailsService.java`
- `src/main/java/com/example/student_topic_management/registration/controller/LecturerRegistrationController.java`
- `src/main/resources/templates/announcements/list.html`
- `src/main/resources/templates/error/405.html`
- `src/main/resources/templates/fragments/navbar.html`
- `src/main/resources/templates/lecturer/registrations/list.html`
- `src/main/resources/templates/lecturers/new.html`
- `src/main/resources/templates/students/new.html`
- `src/main/resources/templates/topics/registration-list.html`
- `src/test/java/com/example/student_topic_management/assessment/controller/CouncilTemplateRenderTest.java`
- `src/test/java/com/example/student_topic_management/assessment/policy/GradePolicyTest.java`
- `src/test/java/com/example/student_topic_management/assessment/policy/ResultRequirementPolicyTest.java`
- `src/test/java/com/example/student_topic_management/assessment/service/DefenseServiceTest.java`
- `src/test/java/com/example/student_topic_management/assessment/service/EvaluationServiceTest.java`
- `src/test/java/com/example/student_topic_management/registration/controller/GroupControllerTest.java`
- `src/test/java/com/example/student_topic_management/registration/service/RoundAccessImplTest.java`

## File chỉ có ở main

Không có trong phạm vi so sánh.

