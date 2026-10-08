# KẾ HOẠCH & THIẾT KẾ ĐỒ ÁN — NHÓM 4 NGƯỜI

Hệ thống quản lý đề tài sinh viên khoa CNTT • Spring Boot MVC + Thymeleaf + Bootstrap 5 + MySQL • Bản thiết kế 1.0 • 06/10/2026

Đây là tài liệu thiết kế và giao việc để nhóm tự triển khai, học và giải thích được sản phẩm; chưa phải báo cáo kết quả đã hoàn thành. A/B/C/D là ký hiệu tạm cho bốn thành viên. Mọi tên file Java/route bên dưới là vị trí dự kiến, phải thay bằng vị trí thực tế trước khi nộp mẫu báo cáo của giảng viên.

## 1. Căn cứ, phạm vi và các điểm cần chốt

Đã đối chiếu đủ 4 tài liệu được gửi: Đồ án cuối kỳ(2).pdf (1 trang); Yêu cầu khi nộp đồ án cuối kỳ(2).pdf (2 trang); Rubric.pdf (4 trang); File báo cáo kết quả dự án của mỗi nhóm.docx. Hai PDF (2) có cùng nội dung văn bản với bản (1) đã gửi trước đó. Căn cứ nghiệp vụ là đề; dùng rubric để bổ sung vai trò, email, minh chứng và kế hoạch bảo vệ.

| Mã | Vấn đề | Cách xử lý trong thiết kế |
| --- | --- | --- |
| C1 | Đề p1: nhóm tối đa3 SV. Rubric p3 và mẫu: nhóm3–5 SV. | Chưa tự kết luận văn bản nào thay thế. Mặc định mỗi đợt min=max=3, đáp ứng phần giao nhau. DB hỗ trợ trần5; Service áp dụng min/max của đợt. Nếu GV chọn đề:1..3; chọn rubric:3..5. Nhóm đang tạo được có ít hơn min nhưng chưa được đăng ký. Nhóm phát triển4 người là chuyện khác. |
| C2 | Yêu cầu nộp p1 đòi deploy; rubric p1 nói không bắt buộc nhưng bảng điểm vẫn đánh giá deploy. | Giữ deploy là mục tiêu trong kế hoạch để đáp ứng mức đầy đủ; xin GV xác nhận ảnh hưởng điểm. Không coi ví dụ nhà cung cấp trong rubric là cam kết có gói miễn phí hay tương thích MySQL. |
| C3 | Đề không nêu Trưởng bộ môn; rubric đòi thể hiện vai trò này. | Bổ sung DEPARTMENT_HEAD và phạm vi bộ môn; sơ duyệt là giả định triển khai cần GV xác nhận. |
| C4 | Đề chỉ nêu công bố trên hệ thống; rubric thêm email từng SV và lọc theo đợt. | Thực hiện cả công bố, email và tra cứu có kiểm tra sở hữu. Không chỉ lọc danh sách ở frontend. |

| Giả định | Quy ước đang dùng | Nếu giảng viên yêu cầu khác |
| --- | --- | --- |
| A1 | GV và SV đều được đề xuất trong pha1; pha2 nhóm chọn danh sách đã công bố. SV đề xuất không tự giữ đề tài. | Đổi quyền tạo đề xuất; không đổi khái niệm đề tài và đăng ký. |
| A2 | Trưởng bộ môn sơ duyệt thuộc bộ môn mình; trưởng khoa duyệt cuối, công bố, phân công. | Thay policy và UC tương ứng; đề không ấn định ai duyệt từng bước. |
| A3 | Nhóm trưởng đăng ký; trưởng khoa xét duyệt. SV tự tham gia nhóm đang FORMING; không có chức năng tự ý thêm người khác. | Có thể thêm lời mời/đồng ý nếu còn thời gian; chưa nằm trong schema v1. |
| A4 | Một đề tài chỉ có một nhóm PENDING/APPROVED trong đợt; pending giữ chỗ; không tự hết hạn. Một nhóm một hồ sơ hiện hành. | Nếu nhiều nhóm/đề tài: bỏ UNIQUE reserved_topic_id, thêm sức chứa và khóa khi đếm. Không chỉ bỏ validation ở giao diện. |
| A5 | Để bao phủ đoạn hội đồng chung của đề: cả4 loại có hội đồng; TLCN/KLTN thêm ít nhất1 GVPB độc lập. GVPB không đồng thời là thành viên hội đồng chấm cùng đề tài. | Đây là chính sách bảo thủ cần chốt. Nếu chỉ KLTN dùng hội đồng, phải sửa điều kiện bắt buộc defense, người confirm và thành phần điểm; schema quan hệ vẫn tái dùng được. |
| A6 | Một người được phân công chấm cho một điểm thành phần trên thang0..10; TB cộng tất cả phiếu bắt buộc đã submit, gồm GVPB và hội đồng; không có điểm GVHD. | Đề chưa nêu rubric điểm con/trọng số/ngưỡng đạt. Không tự thêm hệ số hoặc đạt/rớt. Nếu có điểm tiêu chí, bổ sung criteria và evaluation_items. |
| A7 | SV chỉ có một nhóm đang thực hiện trên toàn hệ thống. Khi kết thúc, đóng nhóm và lưu lịch sử; SV được tham gia đợt mới. | Nếu GV muốn cấm suốt đời hoặc chỉ cấm trong từng đợt, phải thay UNIQUE active_student_id và mô tả. |
| A8 | Đề tài/nhóm/phân công đã có dữ liệu chấm không xóa cứng. Báo cáo chỉ nhận PDF <=10MB theo quy ước nhóm; hạn nộp là trường mở rộng tùy chọn. | Điều chỉnh loại file/giới hạn/hạn theo GV, ghi vào báo cáo; không trình bày như điều bắt buộc của đề. |

## 2. Hướng kiến trúc và phạm vi sản phẩm

Dùng một ứng dụng Spring Boot dạng modular monolith: một mã nguồn, một database, chia package theo chức năng. Giao diện HTML được Thymeleaf render ở server; Bootstrap5 cung cấp CSS/component. Thymeleaf là công nghệ tích hợp với Spring, Bootstrap không phải framework backend của Spring. Không cần SPA hoặc microservices cho quy mô này.

Luồng request: trình duyệt → Spring Security → @Controller nhận form/DTO và @Valid → @Service kiểm tra nghiệp vụ trong transaction → JpaRepository → MySQL. Controller đưa dữ liệu vào Model, trả tên template; Thymeleaf sinh HTML gửi về trình duyệt. Sau POST thành công dùng redirect (Post/Redirect/Get). Validation form báo lỗi ngay tại trường.

| Tầng/kỹ thuật | Công dụng phải giải thích được | Minh chứng dự kiến |
| --- | --- | --- |
| Spring MVC + Thymeleaf | Request mapping, Model, form binding, view và redirect | TopicController + templates/topics/list.html |
| Spring Data JPA | Entity, quan hệ, repository, query và phân trang | TopicRepository, Topic, Department |
| Spring Security | Session authentication, BCrypt, URL/method/ownership authorization, CSRF | SecurityConfig, @EnableMethodSecurity, @PreAuthorize |
| Bean Validation | @NotBlank, @Size, @Email, @DecimalMin/Max; validator liên trường | RoundForm + RoundDatesValidator |
| DI/IoC | Constructor injection; Spring tạo và nối bean | RegistrationService nhận policy/repository/Clock |
| Transaction | Ghi nhiều bảng nguyên tử; rollback khi lỗi, xử lý cạnh tranh | register(), confirm(), publish() |
| Spring Mail/Scheduling | Gửi thông báo sau commit; worker xử lý outbox | MailOutboxWorker + JavaMailSender |

Khởi tạo bằng Spring Initializr, Maven, Java21; chọn bản Spring Boot stable tương thích mà cả nhóm và môi trường deploy cùng dùng, ghi cố định trong pom.xml/Maven Wrapper. Dependency theo tên chức năng: Spring Web/MVC, Thymeleaf, Spring Data JPA, Spring Security, Validation, MySQL Driver, Java Mail Sender; test starter và security-test. Không trộn tên starter của các thế hệ Boot. Lombok tùy chọn; tránh @Data trên entity có quan hệ hai chiều. Chọn một bản Bootstrap5 cụ thể và dùng thống nhất.

Package gốc dự kiến: com.example.studenttopicmanagement. Các package: identity, announcement, topic, registration, assessment, notification, common/config, common/exception, common/audit. Mỗi module có controller, dto, service, repository, entity. Templates chia identity, departments, rounds, topics, groups, registrations, reports, councils, evaluations, results; fragments/layout.html dùng chung. Mọi module phải có backend + giao diện + kiểm thử + đoạn báo cáo của chính mình.

Schema: dùng schema_mysql.sql làm bản khởi tạo, JPA ddl-auto=validate; không để mỗi thành viên tự dùng update và trôi schema. Có thể đưa các CREATE TABLE vào Flyway V1 sau khi DB đã được tạo riêng; không vừa import thủ công vừa chạy lại V1 trên cùng database. JDBC secrets, SMTP secrets, đường dẫn file lấy từ environment; không commit. DATETIME lưu UTC, form hiển thị Asia/Ho_Chi_Minh; đổi múi giờ tại biên, dùng Clock cho kiểm thử thời hạn.

## 3. Chia việc cho bốn người theo module

| Người | Module chính và sản phẩm | Trách nhiệm chung | Người review |
| --- | --- | --- | --- |
| A | M1: tài khoản, GV/SV, bộ môn, phân quyền, thông báo; email worker/audit dùng chung | Khởi tạo, layout, môi trường deploy; hướng dẫn mọi người gọi policy/audit | B review nền tảng; D review quyền/email |
| B | M2: đợt, đề xuất đề tài, sơ duyệt/duyệt/công bố, GVHD, tìm kiếm | Điều phối schema/migration và ghép bản Word; A cùng kiểm tra deploy | C review thời gian; D review GVHD |
| C | M3: nhóm/thành viên, đăng ký/xét duyệt, nộp và tải báo cáo | Dữ liệu demo, checklist E2E; phụ trách file storage khi deploy | A review file/quyền; B review đăng ký |
| D | M4: hội đồng, GVPB, lịch, chấm, tổng hợp, công bố và tra cứu | Kịch bản demo/bảo vệ; A hỗ trợ email, B hỗ trợ màn hình danh sách hội đồng | C review điểm; A review công bố |

Khối lượng mục tiêu gần25%/người, không dùng số bảng hoặc số dòng code để tuyên bố đóng góp thực tế. M4 nặng nghiệp vụ nên A nhận phần gửi email/retry và B nhận việc ghép tài liệu. Cuối dự án điều chỉnh % trong mẫu theo issue/commit/test/Word thực tế, tổng100%. Không giao một người chỉ làm frontend hoặc chỉ làm Word vì vấn đáp cá nhân chiếm3 điểm.

### 3.1. Map triển khai M1 — A

Thứ tự: A01 nền tảng → A02 dữ liệu danh mục → A03 đăng nhập/phân quyền → A04 thông báo → A05 audit/email → A06 deploy. Làm CRUD bộ môn trước để học trọn luồng MVC, sau đó mới cấu hình phân quyền chi tiết.

Controller nhận DTO, không bind trực tiếp User có roles/passwordHash từ form. Service tạo user và hồ sơ GV/SV trong cùng transaction; băm password bằng PasswordEncoder; khóa user thay vì xóa người có lịch sử. DEAN/DEPARTMENT_HEAD đồng thời có hồ sơ lecturer và role LECTURER. Admin quản trị tài khoản nhưng không tự động có quyền duyệt/chấm thay trưởng khoa.

Email là phần phối hợp A–D: D công bố và ghi email_outbox trong cùng transaction; A lấy job sau commit, gửi SMTP, đánh dấu SENT/FAILED và retry có backoff/giới hạn. UNIQUE(result_id,student_id) chống tạo job trùng. Worker dùng lease locked_at và cơ chế thu hồi PROCESSING bị treo; SMTP không bảo đảm exactly-once nên có thể gửi lặp nếu crash sau khi SMTP nhận nhưng trước khi ghi SENT. Không rollback điểm khi SMTP lỗi.

Đầu ra: CRUD danh mục, đăng nhập/logout, trang403/404, template layout, thông báo, worker/email status, policy hỗ trợ, README cấu hình. Kiểm tra xuyên suốt quyền ở server và ownership; sec:authorize chỉ để hiển thị menu.

### 3.2. Map triển khai M2 — B

Thứ tự: B01 đợt → B02 đề xuất → B03 sơ duyệt/duyệt → B04 GVHD → B05 cửa sổ hai pha → B06 đồng bộ thiết kế. Round status chỉ DRAFT/OPEN/CLOSED; trong OPEN tính pha theo thời gian, không phải cron đổi trạng thái mới có hiệu lực.

Bước nghiệp vụ: trưởng khoa tạo đợt draft với4 mốc → validator kiểm tra không chồng pha và trường theo loại → mở đợt → GV/SV tạo và nộp đề xuất trong pha1 → trưởng bộ môn cùng bộ môn sơ duyệt → trưởng khoa kiểm tra1–2 GVHD và duyệt → công bố trước pha2 → SV thấy danh sách có thể đăng ký. Nếu đề xuất bị từ chối, sửa và nộp lại trong pha1, xóa kết quả sơ duyệt cũ và ghi audit.

Hàm hợp đồng: RoundPolicy.requireProposalWindow(round, now); requireStudentWindow(round, now); TopicQueryService.findPublishedTopics(roundId, filter, page); SupervisorService.isSupervisor(topicId, lecturerId). Pha dùng [start,end), nghĩa là đúng giờ bắt đầu được phép, đúng giờ kết thúc bị chặn. Không sửa loại/mốc/min/max sau khi OPEN trong bản v1. Đề tài có đăng ký approved thì khóa nội dung/GVHD; yêu cầu sửa đặc biệt là luồng tương lai.

Đầu ra: màn hình quản lý đợt4 loại, đề xuất và hàng đợi sơ duyệt/duyệt, danh sách đề tài công bố có filter, phân công GVHD. Chứng minh cùng một URL backend từ chối đăng ký sai giờ dù sửa HTML.

### 3.3. Map triển khai M3 — C

Thứ tự: C01 nhóm → C02 thành viên → C03 đăng ký → C04 xét duyệt → C05 báo cáo → C06 kiểm thử tích hợp. Nhóm trưởng là thành viên slot1; tạo nhóm và slot1 trong một transaction, không tạo nhóm rỗng. SV tự tham gia nhóm FORMING trong pha2; không cho người dùng giả studentId của người khác.

Trước gửi đăng ký: phải là trưởng nhóm, round OPEN/đúng pha2, số thành viên đạt min/max, đúng một trưởng nhóm, mỗi SV chỉ thuộc một nhóm active, topic PUBLISHED cùng round. Cả pending và approved đều khóa thành viên; rejected/cancelled mới được chỉnh trong thời hạn. Trưởng nhóm hủy pending để trả đề tài về danh sách; không tự hủy approved trong bản v1.

Đề tài giữ chỗ từ PENDING bằng unique reserved_topic_id. Nếu rejected/cancelled, cột này tự NULL, đề tài được đăng ký lại. UNIQUE(group_id) giữ một hồ sơ cho nhóm; sửa hồ sơ rejected/cancelled thay vì insert dòng thứ2, audit giữ lịch sử. Khi approved khóa nhóm thành LOCKED. Không cập nhật lại topic_id sau khi đã có báo cáo/chấm.

Tệp báo cáo: metadata trong reports, file vật lý/kho riêng ngoài static; key ngẫu nhiên, kiểm tra size và nội dung PDF, không dùng tên gốc làm path. Download qua controller kiểm tra người trong nhóm, GVHD, người được phân công chấm hoặc trưởng khoa. Mỗi lần nộp tạo version_no dưới khóa registration; lưu file tạm → ghi metadata → hoàn tất; nếu transaction lỗi xóa file mới, job dọn file mồ côi. Khi đã bắt đầu chấm hoặc hết hạn mở rộng thì chặn nộp lại.

Đầu ra: màn hình nhóm, thành viên, đăng ký, hàng đợi duyệt, lịch sử báo cáo. Thành viên lịch sử vẫn xem kết quả đợt cũ sau khi nhóm kết thúc; query kết quả không chỉ xét left_at IS NULL.

### 3.4. Map triển khai M4 — D

Thứ tự: D01 hội đồng → D02 phân công/lịch → D03 phiếu chấm → D04 tổng hợp → D05 công bố → D06 demo. Hội đồng draft được chưa đủ3 GV; khi ACTIVE cần3–5 người, slot1 chủ tịch, slot2 thư ký, các slot còn lại là ủy viên. Chủ tịch/thư ký/GVPB/GVHD là vai trò theo phân công, không phải cờ toàn cục cho phép thao tác mọi đề tài.

Chọn hồ sơ approved và có báo cáo; gắn hội đồng cùng round và scheduled_at. KLTN phải đúng council_date; loại khác dùng scheduled_at riêng vì đề chỉ đặt ngày cấp đợt cho KLTN. Khi gắn hội đồng, tạo assignment COUNCIL cho mọi thành viên. TLCN/KLTN thêm REVIEWER theo A5. Chặn GVHD ngay khi phân công, khi chấm và khi tổng hợp. Khóa danh sách hội đồng sau khi đã gắn đề tài trong v1; muốn sửa phải thu hồi lịch/phân công khi chưa có phiếu, có audit.

Phiếu chấm gắn duy nhất một assignment. Điểm dùng BigDecimal,0..10; chỉ chính người được phân công lưu/submit. reviewer_due_at áp dụng REVIEWER cho TLCN/KLTN; không áp cho hội đồng một cách tùy ý. Phiếu đã submit không sửa trong v1. Mọi đường ghi đánh giá/phân công/result phải khóa cùng registration để không thêm/sửa phiếu trong lúc tổng hợp.

Chủ tịch đúng hội đồng gọi confirm: có defense hợp lệ, danh sách assignment khớp chính sách, tất cả phiếu đã submit, không có GVHD, không thiếu nhận xét/kết luận bắt buộc theo quy ước. Điểm cuối = SUM(score)/COUNT(phiếu bắt buộc); làm tròn HALF_UP2 chữ số ở bước cuối. Lưu final_score, component_count, confirmed_by, confirmed_at, conclusion; khóa mọi thành phần. Sau đó trưởng khoa publish một lần, tạo outbox cho từng thành viên và audit cùng transaction. Không tự gán đạt/rớt khi chưa có ngưỡng trong đề.

SV tra cứu bằng principal hiện tại + membership lịch sử + registration.round_id + results.published_at IS NOT NULL. Thay roundId/groupId trên URL không được trả điểm khác. Endpoint tải/xuất và query phụ cũng áp dụng quy tắc này. Sau công bố, trưởng khoa đóng nhóm, cập nhật COMPLETED/closed_at và left_at của thành viên để cho phép đợt sau; lịch sử không xóa.

## 4. Backlog giao việc và phụ thuộc

| Mã | Chủ trì | Hạng mục | Công việc | Phụ thuộc | Map FR | Hoàn thành khi | Review |
| --- | --- | --- | --- | --- | --- | --- | --- |
| A01 | A | Nền tảng | Khởi tạo Maven, thống nhất Java/Boot; cấu hình dev/test/prod, layout Bootstrap | Chưa có | FR01 | Ứng dụng lên trang login và kết nối MySQL | B |
| A02 | A | Tài khoản | Entity/Repository hồ sơ GV/SV, bộ môn, users/roles; CRUD và validation | Schema v1 | FR01 | CRUD có kiểm tra trùng mã/email, khóa tài khoản | C |
| A03 | A | Bảo mật | Session login, BCrypt, SecurityFilterChain, method security, kiểm tra quyền đối tượng | A02 | FR01 | Chặn giả vai trò/đổi ID; không có mật khẩu rõ | D |
| A04 | A | Thông báo | CRUD nháp/công bố; escape nội dung, phân trang | A03 | FR02 | Người dùng chỉ thấy thông báo công bố | B |
| A05 | A | Email/audit | AuditService; JavaMailSender; outbox worker retry và trang trạng thái | D03 hợp đồng ResultPublished | FR18,FR20 | SMTP lỗi không mất công bố; mỗi SV một job | D |
| A06 | A | Deploy/tích hợp | Chủ trì cấu hình deployment + secrets; B hỗ trợ migration, C file storage, D email | Luồng E2E | NFR06 | URL chạy, database/file bền vững, hướng dẫn dựng sạch | C |
| B01 | B | Đợt | Round entity/form, validator loại và thời gian, CRUD draft; open/close | A02 | FR03 | Tạo được 4 loại và từ chối mốc sai | A |
| B02 | B | Đề tài | Topic entity/form, CRUD đề xuất GV/SV; phân trang theo bộ môn/đợt | B01 | FR05,FR19 | Chỉ chủ sở hữu sửa đề xuất nháp | C |
| B03 | B | Phê duyệt | Sơ duyệt trưởng bộ môn → duyệt trưởng khoa → công bố | B02,A03 | FR04,FR07 | Chỉ đề tài đã duyệt mới được công bố | D |
| B04 | B | GVHD | Thêm/xóa GVHD trong nháp; kiểm tra 1–2 lúc duyệt; khóa sau có đăng ký duyệt | B02 | FR06 | Chặn GVHD thứ ba và duyệt đề tài 0 GVHD | D |
| B05 | B | Policy/handoff | RoundPolicy dùng Clock, query đề tài có thể đăng ký; bàn giao C | B01–B04 | FR04 | Kiểm thử đúng mốc mở/đóng; giao diện chỉ pha hợp lệ | C |
| B06 | B | Tài liệu thiết kế | Ghép Ch1/Ch3, thống nhất UC, đề mục và đối chiếu thay đổi schema | Mỗi module có nháp | DOC01 | FR/NFR, UC và code thống nhất | A |
| C01 | C | Nhóm | Tạo nhóm + trưởng nhóm transaction; SV tự tham gia nhóm FORMING | A02,B01 | FR08,FR09 | Slot trưởng nhóm, unique SV đang hoạt động | A |
| C02 | C | Thành viên | Rời nhóm/đổi trưởng nhóm trước đăng ký; khóa khi PENDING/APPROVED | C01 | FR08,FR09 | Chặn vượt max và nhóm thiếu trưởng; giữ lịch sử | D |
| C03 | C | Đăng ký | Khóa đợt, nhóm, đề tài theo thứ tự; kiểm tra pha/số lượng/chủ thể; tạo hồ sơ | B05,C02 | FR10 | Hai nhóm tranh một đề tài chỉ một nhóm thành công | B |
| C04 | C | Xét duyệt | Duyệt/từ chối, hủy pending; nộp lại hồ sơ rejected; audit thay đổi | C03 | FR11 | Mỗi nhóm một hồ sơ, không thay đề tài sau duyệt | B |
| C05 | C | Báo cáo | Upload phiên bản PDF, giới hạn dung lượng, kiểm tra quyền tải file | C04 | FR12 | Nhóm trưởng nộp, SV khác/nhóm khác không nộp hộ | A |
| C06 | C | E2E & dữ liệu | Chuẩn bị các tài khoản/nhóm mẫu, chủ trì checklist toàn luồng và lỗi đồng thời | A–D có chức năng | NFR01,NFR03 | Dữ liệu demo riêng; biên bản test có thực tế | D |
| D01 | D | Hội đồng | Council/member CRUD draft; active khi đủ 3–5 và vai trò hợp lệ | A02,B01 | FR13 | Đúng chủ tịch/thư ký; không trùng GV | B |
| D02 | D | Phân công | Gắn hội đồng/lịch vào hồ sơ; tạo assignments cho từng thành viên + GVPB | C04,D01 | FR14,FR15 | Cùng đợt; chặn GVHD; tránh trùng giờ giảng viên | B |
| D03 | D | Điểm | Form điểm/nhận xét, submit; kiểm tra người chấm và deadline; khóa sau chốt | D02 | FR16 | Điểm 0..10; chỉ sửa nháp của chính mình | C |
| D04 | D | Tổng hợp | Chủ tịch confirm AVG khi đủ phiếu; snapshot score/count/conclusion | D03 | FR17 | Không tổng hợp khi thiếu phiếu; 8,7,9 ra 8 | C |
| D05 | D | Công bố | Trưởng khoa publish; ghi job email cùng transaction; màn hình SV theo round | D04,A05 | FR18 | Không lộ điểm chưa công bố/nhóm khác/đợt khác | A |
| D06 | D | Demo & bảo vệ | Điều phối kịch bản 12–15 phút; mỗi người trình bày chức năng của mình | C06 | DEMO01 | Cả 4 giải thích/sửa được code và annotation | A |

## 5. Ma trận truy vết yêu cầu → module → code → kiểm thử

| Mã | Người | Yêu cầu | Nguồn | Vị trí dự kiến | Test |
| --- | --- | --- | --- | --- | --- |
| FR01 | A | Login/logout, quản lý tài khoản, vai trò; CRUD GV, SV, bộ môn | Đề p1; Rubric p2–3 | SecurityConfig; AccountService; /admin/users | T01–T04 |
| FR02 | A | Đăng và xem thông báo chính thức | Đề p1; Rubric p3 | AnnouncementService; /announcements | T05 |
| FR03 | B | Trưởng khoa tạo đợt đủ loại, mốc thời gian và trường có điều kiện | Đề p1 | RoundService; /dean/rounds | T06–T08 |
| FR04 | B | Tách giai đoạn xây dựng/công bố và giai đoạn SV đăng ký | Đề p1; Rubric p3 | RoundPolicy; TopicService; RegistrationService | T09–T10 |
| FR05 | B | CRUD đề tài theo bộ môn, GV/SV đề xuất, duyệt và công bố | Đề p1; Rubric p3 | TopicService; /topics; /lecturer/topics | T11–T13 |
| FR06 | B | Phân công 1–2 GVHD cho đề tài hợp lệ | Đề p1 | SupervisorService; /dean/topics/{id}/supervisors | T14 |
| FR07 | B | Trưởng bộ môn sơ duyệt đề tài thuộc bộ môn mình | Vai trò: Rubric p2; quyền cụ thể: giả định A2 | DepartmentReviewService; /department/topics | T15 |
| FR08 | C | Lập/tham gia nhóm; đúng một trưởng nhóm; giới hạn thành viên | Đề p1 vs Rubric p3; xem C1 | GroupService; /student/groups | T16–T18 |
| FR09 | C | SV không đồng thời tham gia nhóm A và B | Đề p1; Rubric p3 | MembershipService + UNIQUE active_student_id | T19 |
| FR10 | C | Nhóm trưởng đăng ký một đề tài đã công bố, đúng đợt và thời hạn | Đề p1; quyền đăng ký: giả định A3 | RegistrationService; /student/registrations | T20–T23 |
| FR11 | C | Trưởng khoa duyệt/từ chối đăng ký; khóa nhóm sau duyệt | Đề p1; người duyệt: giả định A3 | RegistrationApprovalService; /dean/registrations | T24 |
| FR12 | C | Chỉ nhóm trưởng nộp báo cáo; người có quyền mới tải được | Đề p1 | ReportService; /student/registrations/{id}/reports | T25–T27 |
| FR13 | D | Thành lập hội đồng 3–5 GV, đúng một chủ tịch và một thư ký | Đề p1; Rubric p3 | CouncilService; /dean/councils | T28–T29 |
| FR14 | D | Phân công GVPB, hội đồng và lịch bảo vệ | Đề p1 | AssignmentService; DefenseService | T30–T31 |
| FR15 | D | GV không chấm đề tài mình hướng dẫn; chấm nhiều đề tài khác được | Đề p1; Rubric p3 | AssessmentPolicy; EvaluationService | T32–T33 |
| FR16 | D | Nhập điểm, nhận xét, chốt phiếu theo phân công và hạn GVPB | Đề p1 | EvaluationService; /lecturer/evaluations | T34–T36 |
| FR17 | D | Chủ tịch tổng hợp, điểm cuối là trung bình cộng | Đề p1; Rubric p3 | ResultService.confirm; /chair/results | T37–T39 |
| FR18 | D+A | Khoa công bố; gửi email từng SV; SV tra cứu đúng đợt và nhóm mình | Đề p1; Rubric p3 | ResultService.publish; MailOutboxService; /student/results | T40–T43 |
| FR19 | B | Tìm kiếm đề tài theo từ khóa, bộ môn, loại/đợt và trạng thái; phân trang | Mở rộng theo Rubric p4 | TopicQueryService; /topics | T44 |
| FR20 | A | Nhật ký thao tác nhạy cảm; theo dõi gửi email lỗi và gửi lại | Thiết kế hỗ trợ truy vết/mở rộng | AuditService; /admin/mail-jobs | T45 |

### 5.1. Yêu cầu phi chức năng và tiêu chí nghiệm thu

| Mã | Yêu cầu | Cách nghiệm thu | Chủ trì |
| --- | --- | --- | --- |
| NFR01 | Bảo mật theo vai trò và đối tượng | Không plaintext; POST thay đổi có CSRF; đổi ID không xem/sửa trái quyền; không log secret | A + mỗi chủ module |
| NFR02 | Nhất quán và toàn vẹn | FK/UNIQUE/CHECK + transaction; không nhóm/đề tài/chấm bị ghi nửa chừng | B/C/D |
| NFR03 | Đúng khi đồng thời | Test2 request tranh SV, đề tài, phiên bản báo cáo, publish; chỉ trạng thái hợp lệ được commit | C/D |
| NFR04 | Dễ sử dụng | Bootstrap5 cùng layout, tên vai trò rõ, lỗi ở trường form, phân trang và filter, thông báo thành công | Mỗi người |
| NFR05 | Khả năng bảo trì | Controller mỏng; Service có policy; DTO; constructor injection; test nghiệp vụ và module boundaries | Mỗi người |
| NFR06 | Có thể dựng và vận hành | Máy sạch chạy theo README; env.example không secret; dữ liệu/files không mất sau restart | A+C |
| NFR07 | Hiệu năng quy mô môn học | Mục tiêu nội bộ: list phân trang <=2s với1000 đề tài trên máy test xác định; đo trước khi tuyên bố đạt | B |
| NFR08 | Truy vết | Audit cho duyệt/từ chối/phân công/điểm/công bố; email lỗi thấy được, có retry | A+D |

## 6. Phân quyền và hợp đồng tích hợp

| Vai trò | Phạm vi cho phép | Không được tự suy ra |
| --- | --- | --- |
| ADMIN | Quản trị user/role/GV/SV/bộ môn, thông báo, mail-job status | Không mặc nhiên được chấm/duyệt thay DEAN |
| DEAN / Trưởng khoa | Đợt, duyệt/công bố đề tài, GVHD, duyệt đăng ký, lập hội đồng/GVPB, công bố điểm | Không được ghi điểm thay người được phân công |
| DEPARTMENT_HEAD / Trưởng bộ môn | Sơ duyệt đề tài thuộc departments của hồ sơ lecturer | Không sơ duyệt bộ môn khác |
| LECTURER / GV | Đề xuất của mình; xem đề tài mình hướng dẫn; chấm assignment của mình | Không chấm mọi đề tài chỉ vì có LECTURER |
| Chủ tịch hội đồng | Lecturer có council_members.slot_no=1 cho defense liên quan: confirm kết quả | Không dùng role CHAIR toàn cục để tổng hợp hội đồng khác |
| Thư ký/ủy viên hội đồng | Chấm assignment và xem lịch/báo cáo được phân công; thư ký xem tiến độ hội đồng | Không tổng hợp thay chủ tịch |
| STUDENT / SV | Đề xuất, xem danh sách công bố, tự tham gia nhóm, tra cứu điểm nhóm/đợt mình | Không thấy điểm tất cả SV trong cùng đợt |
| Nhóm trưởng | STUDENT đang ở slot1 nhóm đó: đăng ký/nộp báo cáo | Không dựa vào role LEADER toàn cục hoặc form gửi lên |

Hợp đồng chung: actorId lấy từ principal, không nhận actorId làm nguồn tin cậy từ hidden input. DomainException cho lỗi nghiệp vụ có thông báo người dùng; AccessDeniedException cho thiếu quyền; @ControllerAdvice trả trang/form phù hợp. DataIntegrityViolationException do tranh chấp được chuyển thành thông báo, không lộ SQL.

Thống nhất thứ tự khóa khi nghiệp vụ chạm nhiều đối tượng: round → group → topic → registration → council theo ID tăng dần trong từng loại. Không phải hàm nào cũng khóa hết; nhưng các hàm sửa cùng một invariant phải dùng cùng parent lock. Khóa round khi cần chống open/close đua với đăng ký có thể làm giảm song song nhưng chấp nhận cho đồ án; chỉ tối ưu sau khi đo. Membership lock thêm student theo ID tăng dần trước group ở các thao tác chỉ thành viên; những thao tác cùng chạm student/group phải dùng cùng thứ tự này, không gọi lồng vào luồng đang giữ group. UNIQUE là hàng rào cuối cùng. Retry có giới hạn cho deadlock/transient conflict, không retry lỗi validation.

Ví dụ register(groupId,topicId): lấy snapshot xác định round → @Transactional khóa round,group,topic → xác thực principal là trưởng nhóm → re-read thành viên/đề tài và requireStudentWindow → kiểm tra size/published/same round → insert hoặc cập nhật hồ sơ rejected → audit → commit. Cả request sửa nhóm và request đăng ký đều khóa group để không đổi thành viên giữa lúc kiểm tra và lưu. Chỉ pre-check exists() mà không lock/unique là chưa đủ.

## 7. Thiết kế CSDL và ranh giới kiểm soát

Bộ schema gồm22 bảng, khóa ngoại có ON DELETE RESTRICT để không xóa dây chuyền lịch sử học tập. Bộ môn là departments. students/lecturers dùng user_id làm PK/FK. Một user có nhiều roles. Các quan hệ nhiều–nhiều có bảng nối và dữ liệu nghiệp vụ riêng. SQL là nguồn chạy chính; DBML dùng vẽ ERD, cột generated được mô tả trong Note vì DBML không tái tạo đầy đủ biểu thức MySQL qua export.

| Module | Bảng | Ý nghĩa |
| --- | --- | --- |
| A | departments | Bộ môn thuộc khoa CNTT; không phải danh sách các khoa. |
| A | users | Tài khoản dùng chung, khóa mềm bằng enabled. |
| A | roles | Vai trò hệ thống; vai trò chủ tịch/thư ký lấy theo hội đồng. |
| A | user_roles | Một người dùng có thể có nhiều vai trò. |
| A | students | Hồ sơ sinh viên; user_id vừa PK vừa FK. |
| A | lecturers | Hồ sơ giảng viên; người chấm/hướng dẫn phải có hồ sơ này. |
| B | registration_rounds | Đợt đăng ký; pha GV/SV suy ra từ cửa sổ thời gian, không từ status. |
| B | topics | Đề tài theo đợt và bộ môn. Giai đoạn 1 nhận đề xuất GV/SV theo giả định A1. |
| B | topic_supervisors | Tối đa 2 GVHD nhờ slot 1..2; tối thiểu 1 tại duyệt do Service bảo đảm. |
| C | student_groups | Nhóm nghiệp vụ theo min/max của đợt; mặc định đúng 3; LOCKED khi được duyệt. |
| C | group_members | Slot 1 là nhóm trưởng. left_at NULL nghĩa là đang tham gia; lịch sử được giữ. |
| C | topic_registrations | Mỗi nhóm một hồ sơ đăng ký; sửa lại hồ sơ bị từ chối và ghi audit, không tạo dòng mới. |
| C | reports | Mỗi lần nộp là một phiên bản; file ở kho riêng, DB lưu metadata. |
| D | councils | Một hội đồng thuộc một đợt và chấm nhiều đề tài. |
| D | council_members | 3–5 GV khi ACTIVE; slot 1 chủ tịch, 2 thư ký, 3–5 ủy viên. |
| D | defenses | Lịch bảo vệ một hồ sơ tại một hội đồng; cùng đợt được FK kép bảo đảm. |
| D | reviewer_assignments | Danh sách người phải chấm: GVPB độc lập hoặc thành viên hội đồng; không trùng người. |
| D | evaluations | Một phiếu cho một phân công; submitted_at NULL là bản nháp. |
| D | results | Bản chốt điểm; sinh viên chỉ thấy khi published_at có giá trị. |
| A | announcements | Thông báo chính thức; chỉ hiển thị bản đã công bố. |
| A | audit_logs | Nhật ký nghiệp vụ, chỉ ghi thêm; không ghi mật khẩu hoặc nội dung file. |
| A | email_outbox | Email công bố kết quả; D ghi cùng transaction công bố, A gửi sau commit và retry. |

| Ràng buộc | MySQL | Spring Service bắt buộc |
| --- | --- | --- |
| SV chỉ1 nhóm đang thực hiện | Generated active_student_id + UNIQUE | Kiểm tra trước để báo lỗi dễ hiểu, transaction khi vào/rời/đóng nhóm |
| Số thành viên/nhóm và đúng1 trưởng | slot1..5 + UNIQUE(group,active_slot); trần vật lý5 | min/max mỗi round; slot1 bắt buộc ở mọi nhóm hợp lệ; khóa chỉnh khi pending/approved |
| Đề tài1–2 GVHD | slot1..2 và unique | Ít nhất1 khi approve/publish; khóa GVHD sau registration approved |
| Hội đồng3–5 | slot1..5 và unique | Ít nhất3 và có slot1/2 trước ACTIVE; thành viên không xung đột GVHD |
| Một nhóm một đề tài | UNIQUE group_id; unique reserved_topic_id theo A4 | Trạng thái, thời gian, quyền; xử lý nộp lại cùng hồ sơ |
| Nhóm/đề tài/hội đồng cùng đợt | FK kép có candidate key(id,round_id) | Kiểm tra sớm trước khi persist |
| Điểm0..10 | DECIMAL + CHECK | Đúng người/hạn; đủ phiếu; cấm GVHD chấm; AVG và khóa sau chốt |
| Công bố/email | Unique result/registration và outbox/result/student | Công bố nguyên tử; gửi sau commit; không công bố thiếu phiếu |

MySQL CHECK không truy vấn bảng khác, nên không thể thay thế Service cho số lượng tối thiểu, vai trò, cửa sổ thời gian và cấm GVHD chấm. SQL có thể chứa trạng thái nháp chưa đủ thành viên/GVHD, nhưng Service không được chuyển sang trạng thái hợp lệ nếu chưa đạt điều kiện. Cột generated map JPA với insertable=false, updatable=false hoặc không map; không set thủ công.

Cần triển khai ứng dụng mới có đầy đủ bảo đảm nghiệp vụ; schema này không tự thực thi quyền hoặc tất cả quy tắc. Không dùng tài khoản root cho ứng dụng; tài khoản DB ứng dụng không nên được người dùng cuối truy cập trực tiếp.

### 7.1. Từ điển dữ liệu

CSV tu_dien_du_lieu.csv chứa đủ156 cột, kiểu, nullable/default và ý nghĩa; mở UTF-8 trong Excel. Danh sách chi tiết cũng nằm ở phụ lục cuối tài liệu này. PK/UNIQUE/FK/CHECK đọc ở SQL và DBML; schema_model.json là mô hình trung gian dùng đồng bộ sơ đồ, không cần nộp nếu không dùng.

## 8. Sơ đồ và cách nhập vào công cụ

so_do_he_thong.drawio là XML draw.io không nén, gồm use case theo4 module, ERD tổng thể, ERD chi tiết theo module, kiến trúc MVC, luồng nghiệp vụ, sequence đăng ký và trạng thái đề tài/đăng ký. Use case dùng actor, ellipse, association và quan hệ tổng quát hóa khi cần; không dùng flowchart thay use case. GVHD/GVPB/chủ tịch/thư ký được biểu diễn riêng dù tài khoản đều là giảng viên.

Draw.io: mở app.diagrams.net hoặc bản desktop → File → Open From → Device → chọn so_do_he_thong.drawio; các trang nằm ở tab phía dưới. File này chính là code XML có thể sửa bằng trình soạn thảo. Khi muốn dán code một trang, mở Extras → Edit Diagram và dùng phần <mxGraphModel> của trang đó; không dán toàn bộ nhiều trang vào một mxGraphModel. Nhãn/menu có thể khác theo ngôn ngữ giao diện; mở file .drawio là cách ưu tiên.

dbdiagram.io (tên miền không có chữ s ở diagrams): tạo diagram → dán toàn bộ erd_dbdiagram.dbml vào editor DBML. Các TableGroup tương ứng4 module. Xuất ảnh để chèn Chương4, kèm DBML gốc khi nộp. Dùng schema_mysql.sql để tạo DB thật, không dùng SQL export từ DBML làm migration chuẩn vì generated columns có thể chỉ còn Note.

Chương3 của hướng dẫn yêu cầu chỉ vẽ use case và liệt kê FR/NFR: đặt các trang use case vào Ch3; ERD vào Ch4; sequence/state/activity/architecture bổ sung đặt Ch2 hoặc phụ lục. Sơ đồ là bản thiết kế, phải cập nhật theo mã nguồn cuối cùng; không đưa hình không khớp code vào báo cáo.

## 9. Kế hoạch thực hiện theo mốc (gợi ý6 tuần)

| Mốc | A | B | C | D | Điều kiện qua mốc |
| --- | --- | --- | --- | --- | --- |
| Tuần1 | Project, security khung, user/role | FR, round/department model | Nhóm model, học transaction | Council/score model, rubric | Chốt C1–C4/A1–A8; schema chung;4 người chạy được app |
| Tuần2 | CRUD account/profile/thông báo | CRUD round/topic/GVHD | Nhóm, thành viên, UI | Hội đồng, thành viên, UI | CRUD demo được; bắt đầu viết Ch1–4 |
| Tuần3 | Phân quyền đầy đủ, audit | Sơ duyệt/duyệt/công bố, policy | Đăng ký/duyệt, test tranh chấp | Phân công reviewer/defense | E2E tới nhóm được duyệt; có negative tests |
| Tuần4 | Email worker + cấu hình | Filter, review cross-module | Báo cáo/file, dữ liệu demo | Chấm/tổng hợp/công bố/query | Happy path đủ và chặn GVHD chấm |
| Tuần5 | Deploy, hỗ trợ security test | Migration, ghép Word | E2E/files/ảnh Ch5 | Email/result concurrency, demo | Chạy máy sạch/deploy; ảnh thật; không lỗi nghiêm trọng |
| Tuần6 | Phụ lục, đóng gói | Rà sơ đồ/FR/code | Rà test/minh chứng mẫu | Tập demo/vấn đáp cùng nhóm | Word+mẫu kết quả+source+sơ đồ+SQL thống nhất |

Nếu ít thời gian hơn, rút thời gian trang trí và mở rộng xuất Excel; không bỏ phân quyền, hai pha, nhóm, hội đồng hoặc bảo đảm chấm đúng. Sau mỗi mốc merge vào nhánh chung. Mỗi task một branch feature/<mã-task>, một PR có mô tả FR/test/screenshot; không commit password/.env/file báo cáo SV thật. Chủ module sửa file mình sở hữu; thay schema phải B điều phối và người bị ảnh hưởng review.

## 10. Chia phần Word và mẫu báo cáo kết quả

| Phần cần nộp | Chủ trì | Người cung cấp/review | Nội dung cụ thể |
| --- | --- | --- | --- |
| Ch1 Tổng quan | B | Cả4; A review | Lý do, mục tiêu, phạm vi, bảng giao việc theo task; phân biệt nhóm lập trình và nhóm SV trong app |
| Ch2 Cơ sở lý thuyết | A | B viết JPA; C viết transaction/validation; D viết Mail; B review | MVC, DI, JPA, Security, Validation, Thymeleaf, transaction: giải thích kèm class/ảnh code thật; >=3 kỹ thuật |
| Ch3 Use case + FR/NFR | B | Mỗi người UC/FR module mình; D review | Actor đủ rubric; use case; bảng FR/NFR; ghi quyết định C1–C4 được GV xác nhận |
| Ch4 CSDL | C | Mỗi người mô tả bảng mình; B review SQL/ERD | ERD tổng thể + chi tiết, PK/FK/cardinality, đủ bảng/cột và ràng buộc DB/Service |
| Ch5 Giao diện | Mỗi người1 mục | C ghép; A kiểm tra format | 5.1 A tài khoản/thông báo;5.2 B đợt/đề tài;5.3 C nhóm/đăng ký/báo cáo;5.4 D hội đồng/điểm/công bố; mỗi ảnh có actor, input, thao tác, kết quả |
| Mục riêng Tính năng mở rộng | D ghép | A email retry/audit; B search; C version report | Ghi rõ cải tiến, lý do, ảnh minh chứng, đường dẫn code; rubric yêu cầu mục riêng để tính mở rộng |
| Ch6 Kết luận | D | Cả4 cung cấp kết quả/lỗi thật; C review | Đạt gì, chưa đạt gì, ưu/khuyết, hướng phát triển; không viết hoàn thành100% nếu chưa test |
| Phụ lục cài đặt/deploy | A | B schema; C file; D SMTP | Java/MySQL/Boot thực dùng, env, migrate, run, account demo, URL, lỗi thường gặp; sơ đồ bổ sung |
| Tài liệu tham khảo | B chuẩn hóa | Mỗi người gửi nguồn mình đã dùng | Slide + tài liệu chính thức; cùng kiểu trích dẫn, ngày truy cập |
| Mẫu báo cáo kết quả nhóm — mục1 | B | Mỗi người xác nhận | Tên/MSSV thật, nhiệm vụ và % thực tế; tổng100%; đúng4 người |
| Mẫu kết quả — mục2 tiến độ/code | C tổng hợp | Mỗi người tự điền dòng phụ trách; người khác mở code kiểm tra | % sau test, tên class/hàm/file/route tồn tại; file dự thảo đi kèm chưa xác nhận vị trí thực tế |
| Mẫu kết quả — mục3 tự đánh giá | D | Cả4 đối chiếu minh chứng | Chỉ tự chấm PhầnI đúng mẫu; PhầnII do GV đánh giá trực tiếp |
| Mẫu kết quả — mục4 bài học | A ghép | Mỗi người viết khó khăn/bài học của mình | Ví dụ thật về transaction, Security, JPA, file/email; không ghi kinh nghiệm chưa trải qua |
| Định dạng/soát cuối | B | A rà kỹ thuật; C rà ảnh/test; D rà rubric | Style Heading, mục lục, caption, số trang; chưa có quy định font cụ thể nên chọn thống nhất và hỏi nếu trường có mẫu |

Hai file Word là hai sản phẩm khác nhau: (1) báo cáo cuối kỳ6 chương; (2) mẫu báo cáo kết quả dự án do giảng viên cung cấp. Không lấy kế hoạch này hoặc mẫu kết quả thay cho báo cáo6 chương. File bao_cao_ket_qua_du_thao.docx là bản sao mẫu để phân công trước; phần tự chấm, MSSV, đóng góp và mức hoàn thành để trống hoặc ghi chưa triển khai.

Mỗi người bàn giao phần Word của mình cùng screenshot thật, caption, đường dẫn code, test và nguồn tham khảo. Không chờ code xong mới viết Ch1–4. Không ký cam kết thay thành viên. Mẫu nhắc rõ không dùng AI sinh code mà không hiểu; vì vậy dùng bản thiết kế này để học và triển khai, mỗi người phải giải thích và sửa được phần mình.

## 11. Test, demo và vấn đáp theo rubric

Có47 test trong test_cases.csv. Đây là test được thiết kế, trạng thái mặc định CHƯA CHẠY. Chạy integration với MySQL thật/container cùng major version; H2 không đủ đại diện generated columns/CHECK/locking của MySQL. Unit test policy bằng Clock cố định; MockMvc/security-test cho CSRF/quyền; integration test concurrency dùng hai transaction thật. Test quan trọng hơn tăng coverage chỉ để đẹp.

| Tiêu chí rubric | Điểm | Minh chứng nhóm cần chuẩn bị | Chủ trì |
| --- | --- | --- | --- |
| Word | 1.0 | 6 chương, phụ lục cài đặt, bảng giao việc, ảnh thật, nguồn | B + cả4 |
| Use case/CSDL | 1.0 | Actor đủ, FR/NFR, ERD/SQL/code khớp, từ điển cột | B+C |
| Kiến trúc | 1.0 | MVC, >=3 kỹ thuật, BCrypt, quyền chi tiết và ownership | A + mỗi người |
| Chức năng | 2.0 | CRUD + hai pha + nhóm + hội đồng + điểm + email + query đúng đợt; deploy | C điều phối test |
| Thuyết trình/demo | 1.0 | Cả4 có phần nói, demo12–15 phút, dữ liệu đã chuẩn bị | D |
| Vấn đáp cá nhân | 3.0 | Mỗi người giải thích luồng MVC, annotation, query, transaction; sửa code trực tiếp | Cả4 |
| Mở rộng/phản biện | 1.0 | Search đa điều kiện, outbox retry, version report, xử lý tranh chấp; mục riêng trong Word | Mỗi người1 minh chứng |

Kịch bản demo gợi ý14 phút: A(3 phút) login4 vai trò, CRUD và thử truy cập trái quyền; B(3 phút) đợt hai pha, đề xuất/sơ duyệt/GVHD/công bố; C(4 phút) nhóm đúng3, chặn SV trùng nhóm, đăng ký và trưởng nhóm nộp; D(4 phút) hội đồng, chặn GVHD, nhập/tổng hợp điểm, công bố/email và SV xem đúng đợt. Chuẩn bị sẵn dữ liệu ở nhiều trạng thái để không phải chờ thời gian thật hoặc nhập tất cả phiếu trong demo. Không giả kết quả test; không lộ mật khẩu SMTP trên màn hình.

| Người | Câu hỏi phải tự giải thích và thực hành |
| --- | --- |
| A | Authentication khác authorization? BCrypt là gì? CSRF vì sao cần cho session form? Vì sao giấu nút không đủ? Email lỗi có làm mất công bố không? |
| B | @Controller trả template khác @RestController thế nào? @ManyToOne/@OneToMany và lazy loading? Validator hai thời điểm? Tại sao test dùng Clock? |
| C | @Transactional ở đâu và rollback khi nào? Hai người cùng đăng ký xử lý ra sao? FK/UNIQUE khác pre-check? Tại sao upload không đặt trong static? |
| D | AVG cần những phiếu nào? Vì sao thiếu một phiếu chưa được confirm? Chặn GVHD ở đâu? Vì sao chủ tịch là quyền theo hội đồng? Truy vấn điểm chống đổi ID thế nào? |

## 12. Chạy schema, deploy và đóng gói

Tạo DB mới: mở MySQL Workbench kết nối server phù hợp → mở schema_mysql.sql → chạy toàn bộ trên DB chưa có bảng. File không DROP và không idempotent cho bảng: lần2 sẽ báo đã tồn tại. Khi dùng CLI: mysql -u <user> -p < schema_mysql.sql (nhập mật khẩu tại prompt). Nếu hosting không cho CREATE DATABASE, DBA tạo database trước và chỉnh/bỏ2 dòng CREATE DATABASE/USE theo tên DB được cấp; giữ các CREATE TABLE. Không chạy vào database đang có dữ liệu thật.

Sau đó cấu hình SPRING_DATASOURCE_URL/USERNAME/PASSWORD; ddl-auto=validate; dùng tài khoản seed tạo bằng PasswordEncoder. Seed tối thiểu:1 admin,1 dean,1 trưởng bộ môn,2 GVHD,5 GV khác làm hội đồng/GVPB,6 SV cho2 nhóm,2 bộ môn,4 loại đợt và các pha khác nhau. Các vai trò trưởng khoa/trưởng bộ môn có thể kiêm giảng viên, nhưng phải kiểm soát xung đột khi demo.

Deploy ứng dụng Spring Boot JAR/container trên nơi hỗ trợ Java, kèm MySQL tương thích và chỗ lưu file bền vững. Tên hosting trong rubric chỉ là ví dụ; cần kiểm tra gói hiện hành, khả năng chạy Java/MySQL, free quota và lưu file trước khi chọn. Không đổi MySQL sang PostgreSQL chỉ để theo một ví dụ trong rubric. Một URL web tĩnh không thay được backend Spring Boot. A làm thử deployment ngay tuần1 để phát hiện giới hạn; tuần5 hoàn thiện. SMTP local có thể dùng hộp thư test; minh chứng gửi thật dùng tài khoản được nhóm cho phép và cấu hình prod riêng.

Checklist nộp: một ZIP chứa report/BaoCaoCuoiKy.docx, report/BaoCaoKetQuaNhom.docx, source/ đầy đủ Maven Wrapper và pom, database/schema+seed nếu có, diagrams/*.drawio+*.dbml, screenshots/, README hướng dẫn và URL hosting. Không bắt buộc PowerPoint; vẫn có thuyết trình/demo/vấn đáp. Bỏ target/, .git/, .env và secrets khỏi ZIP; giữ source/test/config mẫu cần dựng lại.

Tên ZIP cho4 người: GroupXX_MSSV1_MSSV2_MSSV3_MSSV4.zip, thay XX và4 MSSV thật. Rubric có ví dụ3 người nên không thêm MSSV thứ5 giả. Cả rubric và yêu cầu nộp đều cảnh báo sai định dạng tên có thể không được chấm. ZIP thiết kế được cung cấp trong cuộc trao đổi này chưa phải ZIP nộp cuối kỳ.

## 13. Tài liệu kỹ thuật đã đối chiếu

Nguồn đề/rubric/mẫu là4 file của giảng viên nêu ở mục1. Các nguồn kỹ thuật chính thức sau phục vụ cách làm, không thay thế quyết định nghiệp vụ của giảng viên (truy cập06/10/2026):

https://spring.io/guides/gs/serving-web-content/

https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html

https://dev.mysql.com/doc/refman/8.4/en/create-table-check-constraints.html

https://dev.mysql.com/doc/refman/8.4/en/innodb-locking-reads.html

https://dbml.dbdiagram.io/docs/

https://www.drawio.com/

## Phụ lục A. Danh sách cột đầy đủ

### departments — Bộ môn thuộc khoa CNTT; không phải danh sách các khoa.

PK: id. UNIQUE: name.

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| name | varchar(150) | Không | Tên bộ môn |
| description | text | Có | Mô tả |
| created_at | datetime | Không | Thời điểm tạo; quy ước UTC |

### users — Tài khoản dùng chung, khóa mềm bằng enabled.

PK: id. UNIQUE: username; email.

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| username | varchar(100) | Không | Tên đăng nhập |
| password_hash | varchar(255) | Không | Mật khẩu đã băm; không lưu bản rõ |
| full_name | varchar(150) | Không | Họ tên |
| email | varchar(254) | Không | Email liên hệ |
| enabled | boolean | Không | Cho phép đăng nhập |
| created_at | datetime | Không | Thời điểm tạo; quy ước UTC |

### roles — Vai trò hệ thống; vai trò chủ tịch/thư ký lấy theo hội đồng.

PK: id. UNIQUE: code.

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| code | varchar(30) | Không | ADMIN, DEAN, DEPARTMENT_HEAD, LECTURER, STUDENT |

### user_roles — Một người dùng có thể có nhiều vai trò.

PK: user_id, role_id. UNIQUE: không có thêm.

FK: user_id → users(id); role_id → roles(id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| user_id | bigint | Không | Tài khoản |
| role_id | bigint | Không | Vai trò |

### students — Hồ sơ sinh viên; user_id vừa PK vừa FK.

PK: user_id. UNIQUE: student_code.

FK: user_id → users(id); department_id → departments(id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| user_id | bigint | Không | Tài khoản sinh viên |
| student_code | varchar(30) | Không | MSSV |
| class_name | varchar(100) | Có | Lớp |
| department_id | bigint | Không | Bộ môn |

### lecturers — Hồ sơ giảng viên; người chấm/hướng dẫn phải có hồ sơ này.

PK: user_id. UNIQUE: lecturer_code.

FK: user_id → users(id); department_id → departments(id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| user_id | bigint | Không | Tài khoản giảng viên |
| lecturer_code | varchar(30) | Không | Mã giảng viên |
| department_id | bigint | Không | Bộ môn |

### registration_rounds — Đợt đăng ký; pha GV/SV suy ra từ cửa sổ thời gian, không từ status.

PK: id. UNIQUE: không có thêm.

FK: created_by → users(id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| name | varchar(200) | Không | Tên đợt |
| round_type | varchar(20) | Không | COURSE, RESEARCH, SPECIALIZATION, THESIS |
| proposal_starts_at | datetime | Không | Bắt đầu giai đoạn 1 |
| proposal_ends_at | datetime | Không | Kết thúc giai đoạn 1, không bao gồm mốc này |
| student_starts_at | datetime | Không | Bắt đầu giai đoạn 2 |
| student_ends_at | datetime | Không | Kết thúc giai đoạn 2, không bao gồm mốc này |
| reviewer_due_at | datetime | Có | Hạn GVPB: bắt buộc TLCN/KLTN, NULL các loại khác |
| council_date | date | Có | Ngày hội đồng cấp đợt: chỉ KLTN |
| report_due_at | datetime | Có | Mở rộng: hạn nộp báo cáo, nếu giảng viên đồng ý |
| group_min_size | tinyint | Không | Giới hạn tối thiểu chờ xác nhận; mặc định đúng 3 |
| group_max_size | tinyint | Không | Giới hạn tối đa chờ xác nhận; mặc định đúng 3 |
| status | varchar(20) | Không | Trạng thái: DRAFT, OPEN, CLOSED |
| created_by | bigint | Không | Trưởng khoa tạo đợt |
| created_at | datetime | Không | Thời điểm tạo; quy ước UTC |

### topics — Đề tài theo đợt và bộ môn. Giai đoạn 1 nhận đề xuất GV/SV theo giả định A1.

PK: id. UNIQUE: round_id,code; id,round_id.

FK: department_reviewed_by → users(id); round_id → registration_rounds(id); department_id → departments(id); proposed_by → users(id); approved_by → users(id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| round_id | bigint | Không | Đợt |
| department_id | bigint | Không | Bộ môn quản lý |
| code | varchar(40) | Không | Mã đề tài trong đợt |
| title | varchar(255) | Không | Tên đề tài |
| description | text | Không | Nội dung |
| requirements | text | Có | Yêu cầu thực hiện |
| proposed_by | bigint | Không | GV hoặc SV đề xuất |
| status | varchar(20) | Không | Trạng thái: DRAFT, SUBMITTED, APPROVED, REJECTED, PUBLISHED |
| department_decision | varchar(10) | Có | APPROVED/REJECTED: sơ duyệt của trưởng bộ môn |
| department_reviewed_by | bigint | Có | Trưởng bộ môn sơ duyệt |
| department_reviewed_at | datetime | Có | Lúc sơ duyệt |
| department_comment | text | Có | Ý kiến bộ môn |
| approved_by | bigint | Có | Trưởng khoa phê duyệt |
| approved_at | datetime | Có | Thời điểm duyệt |
| rejection_reason | text | Có | Lý do từ chối |
| published_at | datetime | Có | Thời điểm công bố |
| created_at | datetime | Không | Thời điểm tạo; quy ước UTC |

### topic_supervisors — Tối đa 2 GVHD nhờ slot 1..2; tối thiểu 1 tại duyệt do Service bảo đảm.

PK: topic_id, lecturer_id. UNIQUE: topic_id,slot_no.

FK: topic_id → topics(id); lecturer_id → lecturers(user_id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| topic_id | bigint | Không | Đề tài |
| lecturer_id | bigint | Không | GVHD |
| slot_no | tinyint | Không | Vị trí 1 hoặc 2, không phải trọng số |

### student_groups — Nhóm nghiệp vụ theo min/max của đợt; mặc định đúng 3; LOCKED khi được duyệt.

PK: id. UNIQUE: id,round_id.

FK: round_id → registration_rounds(id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| round_id | bigint | Không | Đợt |
| name | varchar(150) | Không | Tên nhóm |
| status | varchar(20) | Không | Trạng thái: FORMING, LOCKED, COMPLETED |
| created_at | datetime | Không | Thời điểm tạo; quy ước UTC |
| closed_at | datetime | Có | Kết thúc thực hiện và giải phóng thành viên |

### group_members — Slot 1 là nhóm trưởng. left_at NULL nghĩa là đang tham gia; lịch sử được giữ.

PK: id. UNIQUE: active_student_id; group_id,active_slot_no; group_id,student_id.

FK: group_id → student_groups(id); student_id → students(user_id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| group_id | bigint | Không | Nhóm |
| student_id | bigint | Không | Sinh viên |
| slot_no | tinyint | Không | 1: trưởng nhóm; 2..5: thành viên; Service áp dụng max của đợt |
| joined_at | datetime | Không | Lúc tham gia |
| left_at | datetime | Có | Lúc rời nhóm hoặc kết thúc đề tài |
| active_student_id | bigint | Có | Cột tính: duy nhất toàn hệ thống khi đang tham gia [generated] |
| active_slot_no | tinyint | Có | Cột tính: vị trí đang dùng trong nhóm [generated] |

### topic_registrations — Mỗi nhóm một hồ sơ đăng ký; sửa lại hồ sơ bị từ chối và ghi audit, không tạo dòng mới.

PK: id. UNIQUE: group_id; reserved_topic_id; id,round_id.

FK: group_id,round_id → student_groups(id,round_id); topic_id,round_id → topics(id,round_id); submitted_by → students(user_id); decided_by → users(id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| group_id | bigint | Không | Nhóm duy nhất |
| topic_id | bigint | Không | Đề tài chọn |
| round_id | bigint | Không | Đợt đồng nhất với nhóm và đề tài |
| status | varchar(20) | Không | Trạng thái: PENDING, APPROVED, REJECTED, CANCELLED |
| submitted_by | bigint | Không | Nhóm trưởng gửi |
| submitted_at | datetime | Không | Thời điểm gửi |
| decided_by | bigint | Có | Trưởng khoa xét duyệt |
| decided_at | datetime | Có | Thời điểm xét duyệt |
| decision_note | text | Có | Lý do duyệt/từ chối |
| reserved_topic_id | bigint | Có | Cột tính: giữ đề tài khi PENDING/APPROVED; giả định một đề tài một nhóm [generated] |

### reports — Mỗi lần nộp là một phiên bản; file ở kho riêng, DB lưu metadata.

PK: id. UNIQUE: registration_id,version_no; storage_key.

FK: registration_id → topic_registrations(id); submitted_by → students(user_id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| registration_id | bigint | Không | Hồ sơ đã duyệt |
| version_no | int | Không | Phiên bản tăng dần |
| storage_key | varchar(255) | Không | Khóa lưu file ngẫu nhiên, không phải URL công khai |
| original_name | varchar(255) | Không | Tên gốc để hiển thị |
| content_type | varchar(100) | Không | Loại nội dung đã kiểm tra |
| size_bytes | bigint | Không | Kích thước |
| sha256 | char(64) | Không | Checksum file |
| submitted_by | bigint | Không | Nhóm trưởng tại thời điểm nộp |
| created_at | datetime | Không | Thời điểm tạo; quy ước UTC |

### councils — Một hội đồng thuộc một đợt và chấm nhiều đề tài.

PK: id. UNIQUE: id,round_id.

FK: round_id → registration_rounds(id); created_by → users(id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| round_id | bigint | Không | Đợt |
| name | varchar(150) | Không | Tên hội đồng |
| status | varchar(20) | Không | Trạng thái: DRAFT, ACTIVE, CLOSED |
| created_by | bigint | Không | Trưởng khoa |
| created_at | datetime | Không | Thời điểm tạo; quy ước UTC |

### council_members — 3–5 GV khi ACTIVE; slot 1 chủ tịch, 2 thư ký, 3–5 ủy viên.

PK: council_id, lecturer_id. UNIQUE: council_id,slot_no.

FK: council_id → councils(id); lecturer_id → lecturers(user_id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| council_id | bigint | Không | Hội đồng |
| lecturer_id | bigint | Không | Giảng viên |
| slot_no | tinyint | Không | 1: chủ tịch; 2: thư ký; 3..5: ủy viên |

### defenses — Lịch bảo vệ một hồ sơ tại một hội đồng; cùng đợt được FK kép bảo đảm.

PK: id. UNIQUE: registration_id.

FK: registration_id,round_id → topic_registrations(id,round_id); council_id,round_id → councils(id,round_id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| registration_id | bigint | Không | Hồ sơ đề tài |
| round_id | bigint | Không | Đợt |
| council_id | bigint | Không | Hội đồng |
| scheduled_at | datetime | Không | Giờ báo cáo |
| location | varchar(200) | Không | Phòng/địa điểm |
| status | varchar(20) | Không | Trạng thái: SCHEDULED, DONE, CANCELLED |

### reviewer_assignments — Danh sách người phải chấm: GVPB độc lập hoặc thành viên hội đồng; không trùng người.

PK: id. UNIQUE: registration_id,lecturer_id.

FK: registration_id → topic_registrations(id); lecturer_id → lecturers(user_id); assigned_by → users(id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| registration_id | bigint | Không | Hồ sơ đề tài |
| lecturer_id | bigint | Không | Người chấm |
| assignment_type | varchar(20) | Không | REVIEWER hoặc COUNCIL |
| assigned_by | bigint | Không | Trưởng khoa |
| created_at | datetime | Không | Thời điểm tạo; quy ước UTC |

### evaluations — Một phiếu cho một phân công; submitted_at NULL là bản nháp.

PK: id. UNIQUE: assignment_id.

FK: assignment_id → reviewer_assignments(id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| assignment_id | bigint | Không | Phân công chấm |
| score | decimal(4,2) | Không | Điểm 0..10; dùng BigDecimal |
| comments | text | Có | Nhận xét |
| submitted_at | datetime | Có | Thời điểm chốt phiếu |
| created_at | datetime | Không | Thời điểm tạo; quy ước UTC |

### results — Bản chốt điểm; sinh viên chỉ thấy khi published_at có giá trị.

PK: id. UNIQUE: registration_id.

FK: registration_id → topic_registrations(id); confirmed_by → lecturers(user_id); published_by → users(id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| registration_id | bigint | Không | Một kết quả mỗi hồ sơ |
| final_score | decimal(4,2) | Không | TB cộng mọi phiếu bắt buộc đã nộp; làm tròn 2 số |
| component_count | int | Không | Số phiếu dùng để tính |
| conclusion | text | Không | Kết luận do chủ tịch xác nhận |
| confirmed_by | bigint | Không | Chủ tịch hội đồng |
| confirmed_at | datetime | Không | Thời điểm tổng hợp |
| published_by | bigint | Có | Trưởng khoa công bố |
| published_at | datetime | Có | NULL: chưa công bố |

### announcements — Thông báo chính thức; chỉ hiển thị bản đã công bố.

PK: id. UNIQUE: không có thêm.

FK: author_id → users(id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| title | varchar(255) | Không | Tiêu đề |
| content | text | Không | Nội dung văn bản; escape khi hiển thị |
| author_id | bigint | Không | Admin hoặc trưởng khoa |
| published_at | datetime | Có | Thời điểm công bố |
| created_at | datetime | Không | Thời điểm tạo; quy ước UTC |

### audit_logs — Nhật ký nghiệp vụ, chỉ ghi thêm; không ghi mật khẩu hoặc nội dung file.

PK: id. UNIQUE: không có thêm.

FK: actor_id → users(id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| actor_id | bigint | Có | Người thực hiện; NULL nếu tác vụ hệ thống |
| action | varchar(60) | Không | Mã thao tác |
| entity_type | varchar(60) | Không | Loại đối tượng, không có FK đa hình |
| entity_id | bigint | Có | ID đối tượng |
| details | text | Có | Tóm tắt thay đổi và lý do |
| created_at | datetime | Không | Thời điểm tạo; quy ước UTC |

### email_outbox — Email công bố kết quả; D ghi cùng transaction công bố, A gửi sau commit và retry.

PK: id. UNIQUE: result_id,student_id.

FK: result_id → results(id); student_id → students(user_id).

| Cột | Kiểu | NULL? | Ý nghĩa |
| --- | --- | --- | --- |
| id | bigint | Không | Khóa chính tự tăng |
| result_id | bigint | Không | Kết quả đã công bố |
| student_id | bigint | Không | Sinh viên nhận |
| recipient_email | varchar(254) | Không | Snapshot email khi công bố |
| subject | varchar(255) | Không | Tiêu đề |
| body | text | Không | Nội dung thông báo và link xem điểm |
| status | varchar(20) | Không | Trạng thái: PENDING, PROCESSING, SENT, FAILED |
| attempts | int | Không | Số lần gửi |
| next_attempt_at | datetime | Có | Lần thử lại kế tiếp |
| locked_at | datetime | Có | Lease worker; phục hồi PROCESSING bị treo |
| sent_at | datetime | Có | Lúc SMTP chấp nhận |
| last_error | varchar(500) | Có | Lỗi đã loại bỏ bí mật |
| created_at | datetime | Không | Thời điểm tạo; quy ước UTC |
