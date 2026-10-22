<!-- Thanh dieu huong -->
| [📄 README](./README.md) | [📋 TASKS A](./TASKS_A.md) | [📋 TASKS B](./TASKS_B.md) | [📋 TASKS C](./TASKS_C.md) | [📋 TASKS D](./TASKS_D.md) |
| :--- | :--- | :--- | :--- | :--- |

# student-topic-management

[FIle EXCEL Tổng hợp](https://docs.google.com/spreadsheets/d/15j5iLNbXE_Gw3CXLfULijvWwq__4noOaoX7NG0bpk4o/edit?usp=sharing)

> [!IMPORTANT]
> Setup .env để cấu hình cho application.
>
> Tạo 1 file .env ngang hàng với file README.md này.
>
> Copy đoạn code dưới vào file .env và thay đổi thông tin theo setup của mình (ví dụ: DB_PASSWORD=123123).

**.env**
```.env
DB_DATABASE=student_topic_management
DB_HOST=localhost
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=12345
PORT=8080
```

> [!IMPORTANT]
> Dùng edge thì chạy link `127.0.0.1:8080/` thay cho `localhost`
```url
127.0.0.1:8080/
```

>[!NOTE]
> Nếu build bị lỗi entity thì chạy lệnh sau: `mvn clean` -> `mvn spring-boot:run`

**cmd**
```cmd
mvn clean && mvn spring-boot:run
```
**powershell**
```powershell
mvn clean; mvn spring-boot:run
```

Tài khoản test local:
| Role | Tài khoản | mật khẩu |
|:-------------|:--------------:|--------------:|
| ADMIN         |    admin      |         admin123 |
| text         |     text       |          text |

## Tài khoản và mật khẩu Demo deploy

Mật khẩu chung cho tất cả tài khoản mẫu: **`Demo@123`**.

| Tài khoản | Vai trò/chức năng |
|---|---|
| `admin` | ADMIN: Quản trị hệ thống. |
| `dean_fit` | DEAN: Trưởng khoa (PGS.TS. Lê Trọng Khoa) - duyệt hồ sơ, công bố kết quả. |
| `gv.minhan` | LECTURER, DEPARTMENT_HEAD: Trưởng bộ môn CNPM, GVHD chính nhiều đề tài. |
| `gv.thanhbinh` | LECTURER: Đồng hướng dẫn đề tài KLTN-T11. |
| `gv.hoangchau`, `gv.ngocdung`, `gv.thihanh` | LECTURER: Thành viên các Hội đồng H01, H02, H03. |
| `gv.quockhanh` | LECTURER: Giảng viên phản biện (Reviewer) độc lập cho H01, H02. |
| `gv.thanhdat` | LECTURER, DEPARTMENT_HEAD: Trưởng bộ môn HTTT, đề xuất các đề tài HTTT. |
| `gv.minhlam` | LECTURER, DEPARTMENT_HEAD: Trưởng bộ môn Mạng máy tính & ATTT. |
| `22110001` đến `22110024` | 24 sinh viên khóa 22 (ví dụ Nguyễn Minh Anh), chia làm 8 nhóm. |