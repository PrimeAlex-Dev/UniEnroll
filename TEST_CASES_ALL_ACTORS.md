# BỘ TÀI LIỆU VÀ KỊCH BẢN KIỂM THỬ TOÀN DIỆN CHO 4 TÁC TỬ (HAPPY-PATH TEST SUITE)
## DỰ ÁN: HỆ THỐNG ĐĂNG KÝ HỌC PHẦN TRỰC TUYẾN (UNIENROLL)
**Trạng thái cơ sở dữ liệu ban đầu:** Database sạch, chỉ khởi tạo duy nhất 4 tài khoản cho 4 vai trò.

---

## 1. DANH SÁCH 4 TÀI KHOẢN MẪU KHỞI TẠO (BASELINE ACCOUNTS)
Mật khẩu mặc định cho toàn bộ 4 tài khoản là: `123456`

| Tác tử (Actor) | Username | Role Spring Security | Mã định danh hồ sơ | Họ và tên | Đơn vị / Chuyên ngành |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Quản trị viên** | `admin` | `ROLE_ADMIN` | N/A | Administrator | Quản trị Hệ thống CNTT |
| **Cán bộ Đào tạo** | `staff` | `ROLE_STAFF` | `CB001` | Nguyễn Văn Đào Tạo | Phòng Đào tạo |
| **Giảng viên** | `lecturer` | `ROLE_LECTURER` | `GV001` | TS. Nguyễn Văn Giảng | Khoa Công nghệ Thông tin |
| **Sinh viên** | `student` | `ROLE_STUDENT` | `SV20260001` | Trần Văn Sinh | K65 - Kỹ thuật Phần mềm |

---

## 2. MA TRẬN 30 TEST CASES THÀNH CÔNG (HAPPY-PATH TEST MATRIX)

### PHẦN 1: TÁC TỬ QUẢN TRỊ VIÊN (SYSTEM ADMINISTRATOR - `admin`)
| Mã Test Case | Use Case | Phương thức & URL | Dữ liệu đầu vào (Payload) | Kết quả mong đợi (Expected Result) |
| :--- | :---: | :--- | :--- | :--- |
| **TC-ADMIN-01** | UC-01 | `POST /api/v1/auth/login` | `{"username":"admin", "password":"123456"}` | Trả về `200 OK`, cấp phát JWT Access Token và Refresh Token. |
| **TC-ADMIN-02** | UC-03 | `GET /api/v1/users/me` | Bearer Token Admin | Trả về thông tin cá nhân `admin`, Role `ROLE_ADMIN`. |
| **TC-ADMIN-03** | UC-04 | `GET /api/v1/admin/users` | Bearer Token Admin | Lấy danh sách toàn bộ người dùng hiện có trong hệ thống ($\ge 4$ users). |
| **TC-ADMIN-04** | UC-04 | `POST /api/v1/admin/users` | `{"username":"staff_test01", "password":"password123", "email":"staff_test01@unienroll.edu.vn", "fullName":"Nguyen Van Test", "role":"ROLE_STAFF"}` | Trả về `201 Created`, tạo thành công tài khoản người dùng mới. |
| **TC-ADMIN-05** | UC-04 | `PUT /api/v1/admin/users/{id}/status` | `{"status":"ACTIVE"}` | Cập nhật trạng thái tài khoản thành `ACTIVE`. |
| **TC-ADMIN-06** | UC-05 | `POST /api/v1/curriculum/departments` | `{"code":"CS", "name":"Khoa Cong nghe Thong tin", "description":"Dao tao Ky thuat Phan mem..."}` | Trả về `201 Created`, tạo Khoa CNTT với mã `CS`. |
| **TC-ADMIN-07** | UC-19 | `GET /api/v1/admin/audit-logs` | Bearer Token Admin | Truy xuất thành công danh sách nhật ký kiểm toán hệ thống. |

---

### PHẦN 2: TÁC TỬ CÁN BỘ PHÒNG ĐÀO TẠO (ACADEMIC STAFF - `staff`)
| Mã Test Case | Use Case | Phương thức & URL | Dữ liệu đầu vào (Payload) | Kết quả mong đợi (Expected Result) |
| :--- | :---: | :--- | :--- | :--- |
| **TC-STAFF-01** | UC-01 | `POST /api/v1/auth/login` | `{"username":"staff", "password":"123456"}` | Trả về `200 OK`, cấp JWT Token cho cán bộ đào tạo. |
| **TC-STAFF-02** | UC-03 | `GET /api/v1/users/me` | Bearer Token Staff | Trả về hồ sơ `StaffProfile`: Mã `CB001`, Tên `Nguyễn Văn Đào Tạo`. |
| **TC-STAFF-03** | UC-05 | `GET /api/v1/curriculum/departments` | Bearer Token Staff | Lấy danh sách các Khoa hiện có (thấy Khoa `CS`). |
| **TC-STAFF-04** | UC-06 | `POST /api/v1/curriculum/courses` | `{"code":"CS101", "name":"Nhap mon Lap trinh", "theoryCredits":3, "practicalCredits":0, "totalCredits":3, "departmentId":deptId}` | Tạo thành công Môn học cơ sở CS101 (3 tín chỉ). |
| **TC-STAFF-05** | UC-06 | `POST /api/v1/curriculum/courses` | `{"code":"CS102", "name":"Cau truc Du lieu va Giai thuat", "theoryCredits":3, "practicalCredits":0, "totalCredits":3, "departmentId":deptId}` | Tạo thành công Môn học nâng cao CS102 (3 tín chỉ). |
| **TC-STAFF-06** | UC-07 | `POST /api/v1/curriculum/courses/{CS102_ID}/prerequisites` | `{"prerequisiteCourseIds":[CS101_ID]}` | Thiết lập quan hệ tiên quyết: CS102 yêu cầu phải học xong CS101. |
| **TC-STAFF-07** | UC-08 | `POST /api/v1/academic/semesters` | `{"code":"2026_HK1", "name":"Hoc ky 1 Nam hoc 2026-2027", "startDate":"2026-09-01", "endDate":"2027-01-15"}` | Khởi tạo thành công Học kỳ mới `2026_HK1`. |
| **TC-STAFF-08** | UC-08 | `POST /api/v1/academic/rooms` | `{"building":"Toa A", "roomNumber":"A101", "capacity":50}` | Tạo phòng học A101 thuộc Tòa A, sức chứa tối đa 50 sinh viên. |
| **TC-STAFF-09** | UC-09 | `POST /api/v1/academic/sections` | `{"sectionCode":"CS101.N01", "courseId":CS101_ID, "semesterId":SEM_ID, "lecturerId":1, "capacity":40}` | Mở Lớp học phần CS101.N01, gán giảng viên `GV001`. |
| **TC-STAFF-10** | UC-10 | `POST /api/v1/academic/sections/{SEC_ID}/slots` | `{"dayOfWeek":1, "startPeriod":1, "endPeriod":3, "roomId":ROOM_ID}` | Xếp lịch học: Thứ 2, Tiết 1-3, tại phòng A101 (không bị xung đột). |
| **TC-STAFF-11** | UC-11 | `PUT /api/v1/academic/sections/{SEC_ID}/capacity` | `{"newCapacity":45}` | Điều chỉnh sức chứa lớp học phần lên 45 ($\le$ sức chứa phòng 50). |
| **TC-STAFF-12** | UC-12 | `POST /api/v1/academic/phases` | `{"semesterId":SEM_ID, "name":"Dot 1...", "startTime":"now - 1d", "endTime":"now + 14d"}` | Kích hoạt Đợt mở cổng đăng ký học phần cho học kỳ hiện hành. |
| **TC-STAFF-13** | UC-18 | `GET /api/v1/reports/occupancy?semesterId={id}` | Bearer Token Staff | Xem báo cáo tỷ lệ lấp đầy lớp học phần trong học kỳ. |

---

### PHẦN 3: TÁC TỬ SINH VIÊN (STUDENT - `student`)
| Mã Test Case | Use Case | Phương thức & URL | Dữ liệu đầu vào (Payload) | Kết quả mong đợi (Expected Result) |
| :--- | :---: | :--- | :--- | :--- |
| **TC-STUDENT-01** | UC-01 | `POST /api/v1/auth/login` | `{"username":"student", "password":"123456"}` | Trả về `200 OK`, cấp JWT Token cho sinh viên. |
| **TC-STUDENT-02** | UC-03 | `GET /api/v1/users/me` | Bearer Token Student | Trả về hồ sơ `StudentProfile`: MSSV `SV20260001`, Tên `Trần Văn Sinh`. |
| **TC-STUDENT-03** | UC-13 | `GET /api/v1/catalog/sections?semesterId={id}` | Bearer Token Student | Tìm kiếm thấy lớp mở `CS101.N01` còn chỗ trống (status: `OPEN`). |
| **TC-STUDENT-04** | UC-14 | `POST /api/v1/registration/enroll` | `{"sectionId":SEC_ID}` | Giao dịch đăng ký thành công theo thời gian thực (status: `ENROLLED`). |
| **TC-STUDENT-05** | UC-16 | `GET /api/v1/registration/my-schedule?semesterId={id}` | Bearer Token Student | Xem Thời khóa biểu cá nhân: Hiển thị lớp CS101.N01 thứ 2 tiết 1-3, tổng tín chỉ = 3 TC. |
| **TC-STUDENT-06** | UC-15 | `POST /api/v1/registration/drop` | `{"sectionId":SEC_ID}` | Hủy đăng ký học phần CS101.N01, hoàn trả 1 chỉ tiêu về lớp (status: `DROPPED`). |
| **TC-STUDENT-07** | UC-16 | `GET /api/v1/registration/my-schedule?semesterId={id}` | Bearer Token Student | Xác nhận Thời khóa biểu sau khi hủy: Tổng số tín chỉ cập nhật về `0 TC`. |

---

### PHẦN 4: TÁC TỬ GIẢNG VIÊN (LECTURER - `lecturer`)
| Mã Test Case | Use Case | Phương thức & URL | Dữ liệu đầu vào (Payload) | Kết quả mong đợi (Expected Result) |
| :--- | :---: | :--- | :--- | :--- |
| **TC-LECTURER-01** | UC-01 | `POST /api/v1/auth/login` | `{"username":"lecturer", "password":"123456"}` | Trả về `200 OK`, cấp JWT Token cho giảng viên. |
| **TC-LECTURER-02** | UC-03 | `GET /api/v1/users/me` | Bearer Token Lecturer | Trả về hồ sơ `LecturerProfile`: Mã GV `GV001`, Tên `TS. Nguyễn Văn Giảng`. |
| **TC-LECTURER-03** | UC-17 | `GET /api/v1/lecturer/sections/{SEC_ID}/roster` | Bearer Token Lecturer | Xem danh sách sinh viên chính thức của lớp được phân công giảng dạy (Class Roster). |

---

## 3. HƯỚNG DẪN KHỞI CHẠY HỆ THỐNG

### Bước 1: Khởi động Cơ sở dữ liệu
Đảm bảo Microsoft SQL Server đang chạy tại `localhost:1433`, kết nối đến database `unienroll_db`.

### Bước 2: Khởi động Backend Spring Boot
Mở terminal tại thư mục backend:
```bash
cd d:\UniEnroll\backend
mvn spring-boot:run
```
* Backend khởi động tại cổng **8080** (`http://localhost:8080`).
* [DataInitializer.java](file:///d:/UniEnroll/backend/src/main/java/com/unienroll/config/DataInitializer.java) sẽ tự động kiểm tra và chỉ khởi tạo đúng 4 tài khoản mặc định nếu chưa tồn tại.

### Bước 3: Khởi động Frontend React (Vite)
Mở một terminal khác tại thư mục frontend:
```bash
cd d:\UniEnroll\frontend
npm run dev
```
* Frontend khởi động tại cổng **3000** (`http://localhost:3000`).

### Bước 4: Kiểm thử chức năng
* **Qua Giao diện Web:** Mở trình duyệt vào `http://localhost:3000` và sử dụng 4 nút "Chọn nhanh tài khoản" trên màn hình đăng nhập để kiểm thử từng vai trò.
* **Qua REST Client (cURL / Postman / Thunder Client):** Gọi tuần tự các API theo ma trận 30 Test Cases ở Mục 2 để thực hiện chu trình đào tạo hoàn chỉnh.
