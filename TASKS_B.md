# Phần B – Quản lý đợt và đề tài

**Người thực hiện:** Lam · **Nhánh:** `Lam`

## B01 – Quản lý đợt đăng ký

- [x] Tạo, sửa, xóa đợt nháp; mở/đóng đợt và phân trang.
- [x] Kiểm tra loại đợt, lịch hai pha và số thành viên.
- [x] Phân quyền DEAN, khóa ghi và chuyển giờ Việt Nam/UTC.

## B02 – Đề xuất đề tài

- [x] GV/SV tạo, sửa, xóa đề xuất DRAFT của mình.
- [x] Gửi SUBMITTED; sửa/nộp lại REJECTED trong pha đề xuất.
- [x] Kiểm tra chủ sở hữu, trạng thái, thời gian và mã trùng trong đợt.
- [x] Chuẩn hóa mã; danh sách cá nhân phân trang, chọn đợt/bộ môn.
- [x] Lưu audit/snapshot trước khi reset kết quả duyệt cũ.

## B03 – Duyệt và công bố đề tài

- [x] B03.1: trưởng bộ môn sơ duyệt SUBMITTED thuộc bộ môn mình.
- [x] Đồng ý giữ SUBMITTED; từ chối có lý do và chuyển REJECTED.
- [x] B03.2: DEAN duyệt đề tài đã được bộ môn đồng ý, chưa có quyết định cuối.
- [x] Kiểm tra 1–2 GVHD; lưu approved_by/approved_at và chuyển APPROVED.
- [x] Từ chối có lý do, lưu rejection_reason và chuyển REJECTED.
- [x] B03.1/B03.2 chỉ xử lý trong pha đề xuất của đợt OPEN.
- [x] B03.3: DEAN công bố từng APPROVED thành PUBLISHED, lưu published_at UTC.
- [x] Chặn duyệt/công bố lặp; dùng khóa ghi, transaction, CSRF và audit.
- [x] Có danh sách, chi tiết và biểu mẫu Thymeleaf.

## B04 – Phân công giảng viên hướng dẫn

- [x] DEAN thêm/xóa GVHD khi đề tài DRAFT.
- [x] Kiểm tra giảng viên hoạt động, vai trò, slot 1–2 và trùng phân công.
- [x] Chặn sửa khi có đăng ký APPROVED.
- [x] B03.2 dùng requireSupervisorCountForApproval trong cùng transaction.

## B05 – Policy và tra cứu công bố

- [x] RoundPolicy kiểm tra OPEN và khoảng [start,end) theo UTC.
- [x] Tra cứu chỉ lấy PUBLISHED; tìm theo mã/tên, chuẩn hóa và escape LIKE.
- [x] Kết hợp lọc đợt/bộ môn; phân trang DB, sắp code ASC/id ASC.
- [x] Mặc định page=0, size=10; giới hạn tối đa 100 bản ghi/trang.
- [x] DTO không chứa thông tin duyệt/chủ sở hữu; chuyển trang giữ bộ lọc.
- [x] Người đã đăng nhập được tra cứu; tách biệt đăng ký của Khải.

## B06 – Tài liệu đã cập nhật

- [x] Đồng bộ Markdown về tiến độ, luồng xử lý, endpoint và kết quả test.
- [x] Đối chiếu DDL hiện có: 23 bảng, 160 cột, 42 FK.
- [x] Xác định bảng phần B: registration_rounds, topics, topic_supervisors.

## URL chính

| Chức năng | URL | Quyền |
| --- | --- | --- |
| Đợt đăng ký | /dean/rounds | DEAN |
| Đề xuất cá nhân | /topics/my | LECTURER/STUDENT |
| Sơ duyệt | /department/topics | DEPARTMENT_HEAD |
| Phê duyệt cuối | /dean/topics/review | DEAN |
| Công bố | /dean/topics/publish | DEAN |
| Phân công GVHD | /dean/topics/supervisors | DEAN |
| Tra cứu công bố | /catalog/topics | Đã đăng nhập |

## Kiểm thử và lệnh chạy

- Compile thành công; **192 PASS, 0 FAIL/ERROR/SKIPPED**.
- B03.1: 47 · B03.2: 66 · B03.3: 26 · B05: 53.
- Đã kiểm tra service, quyền, CSRF và render Thymeleaf bằng MockMvc.

```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd '-Dtest=TopicPublication*B033Test,DeanTopicReview*B032Test,DepartmentReview*B03Test,PublishedTopic*B05Test' test
.\mvnw.cmd spring-boot:run
```

## Lưu ý cần thiết

- Test trên dùng repository mock, chưa xác minh JPQL/MySQL thật; không phải full test.
- B03.3 cơ bản chưa kiểm tra lại pha thời gian/GVHD; B05 không bảo đảm còn chỗ đăng ký.
- B06 chưa hoàn tất: ERD/DBML vẫn thiếu registration_members; cấu hình chạy xem [README](README.md).