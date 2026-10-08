# Bộ thiết kế đồ án quản lý đề tài — nhóm 4 người

Đọc `Ke_hoach_va_thiet_ke.docx` hoặc bản Markdown trước. Đây là bộ thiết kế và kế hoạch, chưa phải ứng dụng Spring Boot đã hoàn thành hay báo cáo kết quả để nộp ngay.

## Tệp chính

- `Ke_hoach_va_thiet_ke.docx`: phân tích4 tài liệu, mâu thuẫn/giả định, kiến trúc, chia A/B/C/D, map từng module, backlog24 task, FR/NFR, phân quyền, kế hoạch6 tuần, phân công Word,47 tình huống test và từ điển dữ liệu.
- `Ke_hoach_va_thiet_ke.md`: cùng nội dung để tìm kiếm/chỉnh sửa.
- `Bao_cao_ket_qua_du_thao.docx`: bản sao mẫu giảng viên, đã phân công4 người. Tên/MSSV/%/tự chấm/minh chứng vẫn chờ thực tế. Không thay cho Word6 chương.
- `schema_mysql.sql`: DDL MySQL8.0.16+ (khuyến nghị kiểm thử MySQL8.4),22 bảng,156 cột,40 FK. Chạy vào database mới; không có DROP. JPA dùng ddl-auto=validate.
- `erd_dbdiagram.dbml`: dán toàn bộ vào editor của https://dbdiagram.io/ để vẽ ERD. Không dùng SQL export từ DBML thay cho DDL vì generated columns được mô tả ở Note.
- `so_do_he_thong.drawio`: XML không nén; mở từ File → Open From → Device trong draw.io.13 trang gồm4 use case, ERD tổng thể,4 ERD chi tiết, kiến trúc, luồng, sequence, state. Có thể sửa XML trực tiếp.
- `phan_cong_cong_viec.csv`:24 task, người làm/review, phụ thuộc, FR và tiêu chí hoàn thành.
- `test_cases.csv`:47 tình huống, chưa chạy; điền kết quả và ảnh/log thật.
- `tu_dien_du_lieu.csv`:156 cột với kiểu và ý nghĩa; UTF-8 BOM để mở Excel.
- `schema_model.json`: mô hình trung gian đối chiếu schema; không bắt buộc nộp.
- `Kiem_tra_thiet_ke.txt`: mức kiểm chứng và giới hạn của bộ file.

## Ba điểm phải đọc trước khi code

1. Nhóm phát triển có4 người. Nhóm SV trong app: đề tối đa3, rubric/mẫu3–5. Thiết kế mặc định min=max=3; cần giảng viên chốt. DB trần5, Service áp dụng min/max mỗi đợt, không đổi sau OPEN.
2. Trưởng bộ môn và email từng SV được bổ sung theo rubric. Quyền sơ duyệt bộ môn, một đề tài/một nhóm và thành phần điểm là giả định được đánh dấu trong tài liệu, không giả làm nội dung đề đã quy định.
3. Yêu cầu nộp nói deploy bắt buộc; rubric nói không bắt buộc nhưng bảng điểm vẫn đánh giá. Kế hoạch vẫn làm deploy và hỏi giảng viên cách tính điểm.

## Bắt đầu cùng nhóm

- Điền người thật vào A/B/C/D; xác nhận các mâu thuẫn/giả định với giảng viên.
- Chốt sơ đồ và schema dùng chung, khởi tạo Maven + Spring Boot MVC + Thymeleaf + Bootstrap5 + MySQL.
- Cả4 học một CRUD bộ môn qua Controller → Service → Repository → MySQL → Thymeleaf.
- Mỗi người tự làm và giải thích module của mình; code, kiểm thử và Word tiến hành song song.
- Chỉ điền mức hoàn thành và vị trí code vào mẫu kết quả sau khi mã đó tồn tại và đã kiểm tra.

Bộ ZIP này là tài liệu thiết kế, KHÔNG phải ZIP nộp cuối kỳ. Tên ZIP cuối cùng phải có GroupXX và4 MSSV thật, chứa code/Word hoàn chỉnh và sản phẩm thực tế.
