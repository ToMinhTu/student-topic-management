# Dữ liệu demo cho defaultdb

Bộ dữ liệu được thiết kế theo đúng file `schema_mysql(3).sql` bạn gửi ngày 21/10/2026: 23 bảng, có `registration_members.id`, không có `registration_members.captured_at`[cite: 4]. Không thay đổi schema[cite: 4].

## 1. Cách nạp

1. Kết nối MySQL và bảo đảm 23 bảng trong `defaultdb` đã được tạo từ schema của bạn[cite: 4]. Nếu bảng đã có thì không chạy lại toàn bộ CREATE TABLE[cite: 4].
2. Dừng ứng dụng trong lúc seed để tránh thao tác khác xen vào[cite: 4]. Dùng profile demo với worker email tắt hoặc SMTP sandbox khi thử công bố kết quả[cite: 4].
3. Giải nén ZIP[cite: 4]. Chép thư mục `reports/` vào kho báo cáo của ứng dụng theo mục 4[cite: 3, 4].
4. Mở file `seed_demo_defaultdb_v2_2.sql` bằng MySQL Workbench (hoặc DBeaver) và chạy **toàn bộ script**, không chỉ câu INSERT được bôi chọn[cite: 3, 4]. File có `USE defaultdb`, `SET NAMES utf8mb4`, `SET SESSION time_zone = '+00:00'`[cite: 3].
5. Xem kết quả `KHOI TAO DU LIEU THANH CONG`, bảng tài khoản và danh sách đăng ký đề tài ở cuối script[cite: 3]. Sau đó chạy ứng dụng và đăng nhập tài khoản mẫu[cite: 4].

Ví dụ chạy bằng MySQL CLI trên máy đã cấu hình kết nối phù hợp[cite: 4]:

bash
mysql --default-character-set=utf8mb4 -u TEN_USER -p defaultdb < seed_demo_defaultdb_v2_2.sql


Với DB từ xa, thêm host/port/TLS theo cấu hình kết nối đang dùng[cite: 4]. Không ghi password thật vào lệnh hoặc commit vào Git[cite: 4].

Script dùng stored procedure để rollback toàn bộ dữ liệu seed nếu gặp lỗi[cite: 4]. Tài khoản SQL cần quyền `CREATE ROUTINE`, `ALTER ROUTINE`, `EXECUTE`, `SELECT`, `INSERT`[cite: 4]. Helper procedure được xóa sau khi chạy thành công[cite: 3, 4]. Không đưa nguyên file này vào `src/main/resources/data.sql` vì `DELIMITER` là lệnh của client, không phải SQL gửi trực tiếp qua JDBC[cite: 4].

## 2. Tài khoản và mật khẩu

Mật khẩu chung cho tất cả tài khoản mẫu: **`Demo@123`**[cite: 4]. Cột `password_hash` lưu BCrypt đã tạo và kiểm tra thực, không lưu mật khẩu dạng rõ[cite: 3, 4]. Đây là mật khẩu demo công khai, không dùng cho tài khoản thật[cite: 4].

| Tài khoản | Vai trò/chức năng |
|---|---|
| `admin` | ADMIN: Quản trị hệ thống[cite: 3]. |
| `dean_fit` | DEAN: Trưởng khoa (PGS.TS. Lê Trọng Khoa) - duyệt hồ sơ, công bố kết quả[cite: 3]. |
| `gv.minhan` | LECTURER, DEPARTMENT_HEAD: Trưởng bộ môn CNPM, GVHD chính nhiều đề tài[cite: 3]. |
| `gv.thanhbinh` | LECTURER: Đồng hướng dẫn đề tài KLTN-T11[cite: 3]. |
| `gv.hoangchau`, `gv.ngocdung`, `gv.thihanh` | LECTURER: Thành viên các Hội đồng H01, H02, H03[cite: 3]. |
| `gv.quockhanh` | LECTURER: Giảng viên phản biện (Reviewer) độc lập cho H01, H02[cite: 3]. |
| `gv.thanhdat` | LECTURER, DEPARTMENT_HEAD: Trưởng bộ môn HTTT, đề xuất các đề tài HTTT[cite: 3]. |
| `gv.minhlam` | LECTURER, DEPARTMENT_HEAD: Trưởng bộ môn Mạng máy tính & ATTT[cite: 3]. |
| `22110001` đến `22110024` | 24 sinh viên khóa 22 (ví dụ Nguyễn Minh Anh), chia làm 8 nhóm[cite: 3]. |

Email giảng viên dùng đuôi `@hcmute.edu.vn` (ví dụ `minhan@hcmute.edu.vn`), email sinh viên dùng đuôi `@student.hcmute.edu.vn` (ví dụ `22110001@student.hcmute.edu.vn`)[cite: 3]. Đăng nhập bằng username hay email tùy code xác thực của hệ thống[cite: 4].

Mặc định SQL tương thích bean `BCryptPasswordEncoder` trực tiếp[cite: 4]. Nếu project dùng `PasswordEncoderFactories.createDelegatingPasswordEncoder()`, **trước khi chạy lần đầu** đổi[cite: 4]:
sql
SET @demo_password_prefix = '{bcrypt}';
trong file SQL[cite: 4]. Nếu dùng BCryptPasswordEncoder trực tiếp, giữ `''`[cite: 3, 4].

## 3. Các kịch bản có sẵn

Ngày gốc D là 00:00 ngày import tại môi trường triển khai[cite: 3, 4].

| Nhóm / Hồ sơ | Tài khoản SV | Dữ liệu sẵn và thao tác nên demo |
|---|---|---|
| Nhóm 01 - ĐAMH | `22110001` - `22110003` | Nhóm FORMING; chưa có hồ sơ đăng ký đề tài[cite: 3]. |
| Nhóm 02 - ĐAMH | `22110004` - `22110006` | Hồ sơ PENDING (910002) cho đề tài CNPM-T02; chờ DEAN duyệt[cite: 3]. |
| Nhóm 03 - ĐAMH | `22110007` - `22110009` | Hồ sơ REJECTED (910003); nhóm có thể cập nhật để nộp lại[cite: 3]. |
| Nhóm 04 - ĐACN | `22110010` - `22110012` | Đã APPROVED (910004), có nộp file PDF báo cáo, chưa phân công[cite: 3]. |
| Nhóm 05 - ĐACN | `22110013` - `22110015` | Lịch bảo vệ SCHEDULED (910005); có phiếu chấm nháp nhưng chưa đủ điểm[cite: 3]. |
| Nhóm 06 - KLTN | `22110016` - `22110018` | Bảo vệ DONE (910006), đã có đủ 4 phiếu điểm 8/7/9/6, chưa tổng kết[cite: 3]. |
| Nhóm 07 - KLTN | `22110019` - `22110021` | Đã confirm điểm 8.50 (910007), chưa công bố; đăng nhập DEAN để publish[cite: 3]. |
| Nhóm 08 - KLTN | `22110022` - `22110024` | Nhóm COMPLETED (910008), đã công bố kết quả 8.50, membership kết thúc[cite: 3]. |

Tên đầy đủ của sinh viên tương ứng với MSSV[cite: 3]. Trưởng nhóm luôn là sinh viên có slot_no = 1[cite: 3]. Chủ tịch/thư ký lấy từ `council_members.slot_no`[cite: 3]. 

H01 (910001) là hội đồng ACTIVE của đợt Đồ án chuyên ngành; H02 (910002) ACTIVE của đợt KLTN; H03 (910003) DRAFT đang dự kiến thành lập[cite: 3]. Giảng viên hướng dẫn không nằm trong hội đồng/phản biện của đề tài họ hướng dẫn[cite: 3, 4].

## 4. Báo cáo PDF và kho file

SQL thêm 5 dòng `reports` có `storage_key`, `size_bytes`, `sha256` khớp chính xác 5 file[cite: 3, 4]:
text
reports/DACN_Nhom04_BaoCao.pdf
reports/DACN_Nhom05_BaoCao.pdf
reports/KLTN_Nhom06_BaoCao.pdf
reports/KLTN_Nhom07_BaoCao.pdf
reports/KLTN_Nhom08_BaoCao.pdf

Ví dụ root kho file của ứng dụng là `D:/student-topic/uploads`, thì file của Nhóm 04 phải nằm tại `D:/student-topic/uploads/reports/DACN_Nhom04_BaoCao.pdf`[cite: 3, 4]. Không sửa nội dung PDF sau khi import nếu chưa cập nhật size/hash tương ứng[cite: 4].

## 5. Email và múi giờ

Có 3 job email outbox cho Nhóm 08, đều ở trạng thái FAILED với thông báo lỗi mẫu[cite: 3]. Khi demo publish kết quả, ứng dụng có thể tạo job PENDING mới[cite: 4].

`SET SESSION time_zone = '+00:00'` đặt timezone cho phiên import thành UTC[cite: 3]. DATETIME không tự động chuyển đổi múi giờ, do đó dữ liệu ngày giờ lưu trữ sẽ theo quy ước UTC mặc định của phiên này[cite: 3, 4]. Cấu hình ứng dụng của bạn (JDBC/JPA) cần nhận thức chuẩn giờ đang ghi vào DB để render đúng giờ[cite: 4].

## 6. Dữ liệu cũ, chạy lại và kiểm chứng

Script chỉ INSERT, không DELETE/TRUNCATE/UPDATE dữ liệu nghiệp vụ[cite: 4]. Dải ID 910000–910999 được dành cho demo; nếu bảng đã có ID trong dải này, script sẽ kích hoạt ROLLBACK và dừng lại[cite: 3, 4]. Không chạy lại để “làm mới ngày” vì sẽ làm sai lịch sử tiến độ[cite: 4].