## 🏛️ MA TRẬN TỔNG HỢP 52 USE CASE ĐÁNH SỐ NGUYÊN THEO ROLE & CRUD

| STT | Mã Use Case | Tên Thao tác / Mục tiêu Nghiệp vụ | Tác tử Chính (Primary Actor) | Phân hệ (Module) | Thành viên Phụ trách | HTTP Method & API Endpoint | Phân quyền Spring Security |
| :---: | :---: | :--- | :--- | :--- | :--- | :--- | :--- |
| **I** | | **MODULE 1: AUTH, SECURITY & USER IDENTITY** | | **Module 1** | **Luân Lư** | | |
| 1 | **UC-01** | Đăng nhập Hệ thống & Cấp phát JWT | Toàn bộ 4 Actors | Module 1 | Luân Lư | `POST /api/v1/auth/login` | `permitAll()` |
| 2 | **UC-02** | Làm mới Access Token (Token Refresh) | Toàn bộ 4 Actors (Axios) | Module 1 | Luân Lư | `POST /api/v1/auth/refresh` | `permitAll()` |
| 3 | **UC-03** | Tra cứu Hồ sơ Người dùng Hiện hành | Toàn bộ 4 Actors | Module 1 | Luân Lư | `GET /api/v1/users/me` | `isAuthenticated()` |
| 4 | **UC-04** | Khởi tạo Người dùng Mới & Gán Role | Quản trị viên (Admin) | Module 1 | Luân Lư | `POST /api/v1/admin/users` | `hasRole('ADMIN')` |
| 5 | **UC-05** | Cập nhật Trạng thái Tài khoản (Khóa/Mở) | Quản trị viên (Admin) | Module 1 | Luân Lư | `PUT /api/v1/admin/users/{id}/status` | `hasRole('ADMIN')` |
| 6 | **UC-06** | Tra cứu & Lọc Danh sách Người dùng | Admin, Academic Staff | Module 1 | Luân Lư | `GET /api/v1/admin/users` | `hasAnyRole('ADMIN', 'STAFF')` |
| 7 | **UC-07** | Xem Chi tiết Hồ sơ Người dùng theo ID | Admin, Academic Staff | Module 1 | Luân Lư | `GET /api/v1/admin/users/{id}` | `hasAnyRole('ADMIN', 'STAFF')` |
| **II**| | **MODULE 2: CURRICULUM & ACADEMIC CATALOG** | | **Module 2** | **Tôi (User)** | | |
| 8 | **UC-08** | Thêm mới Khoa / Bộ môn | Admin, Academic Staff | Module 2 | Tôi (User) | `POST /api/v1/admin/departments` | `hasAnyRole('ADMIN', 'STAFF')` |
| 9 | **UC-09** | Cập nhật Thông tin Khoa / Bộ môn | Admin, Academic Staff | Module 2 | Tôi (User) | `PUT /api/v1/admin/departments/{id}` | `hasAnyRole('ADMIN', 'STAFF')` |
| 10| **UC-10** | Xóa Khoa / Bộ môn khỏi Hệ thống | Admin, Academic Staff | Module 2 | Tôi (User) | `DELETE /api/v1/admin/departments/{id}` | `hasAnyRole('ADMIN', 'STAFF')` |
| 11| **UC-11** | Tra cứu & Xem Danh sách Khoa | Toàn bộ 4 Actors | Module 2 | Tôi (User) | `GET /api/v1/admin/departments` | `isAuthenticated()` |
| 12| **UC-12** | Thêm mới Môn học vào Khung CTĐT | Cán bộ Đào tạo (Staff) | Module 2 | Tôi (User) | `POST /api/v1/courses` | `hasRole('STAFF')` |
| 13| **UC-13** | Cập nhật Thông tin & Tín chỉ Môn học | Cán bộ Đào tạo (Staff) | Module 2 | Tôi (User) | `PUT /api/v1/courses/{id}` | `hasRole('STAFF')` |
| 14| **UC-14** | Xóa Môn học khỏi Hệ thống | Cán bộ Đào tạo (Staff) | Module 2 | Tôi (User) | `DELETE /api/v1/courses/{id}` | `hasRole('STAFF')` |
| 15| **UC-15** | Tra cứu Danh mục Môn học & Chi tiết CTĐT | Sinh viên, Giảng viên, Staff | Module 2 | Tôi (User) | `GET /api/v1/courses` | `isAuthenticated()` |
| 16| **UC-16** | Thiết lập / Gán Điều kiện Môn Tiên quyết | Cán bộ Đào tạo (Staff) | Module 2 | Tôi (User) | `POST /api/v1/courses/{id}/prerequisites` | `hasRole('STAFF')` |
| 17| **UC-17** | Gỡ bỏ Quan hệ Môn Tiên quyết | Cán bộ Đào tạo (Staff) | Module 2 | Tôi (User) | `DELETE /api/v1/courses/{id}/prerequisites/{pId}` | `hasRole('STAFF')` |
| 18| **UC-18** | Tra cứu Cây Môn Tiên quyết của Môn học | Toàn bộ 4 Actors | Module 2 | Tôi (User) | `GET /api/v1/courses/{id}/prerequisites` | `isAuthenticated()` |
| **III**| | **MODULE 3: SCHEDULING & INFRASTRUCTURE** | | **Module 3** | **Thành Huy** | | |
| 19| **UC-19** | Thêm mới Học kỳ Đào tạo | Cán bộ Đào tạo (Staff) | Module 3 | Thành Huy | `POST /api/v1/academic/semesters` | `hasRole('STAFF')` |
| 20| **UC-20** | Cập nhật Thông tin Học kỳ | Cán bộ Đào tạo (Staff) | Module 3 | Thành Huy | `PUT /api/v1/academic/semesters/{id}` | `hasRole('STAFF')` |
| 21| **UC-21** | Chuyển đổi Trạng thái Học kỳ (Kích hoạt/Đóng) | Cán bộ Đào tạo (Staff) | Module 3 | Thành Huy | `PATCH /api/v1/academic/semesters/{id}/status` | `hasRole('STAFF')` |
| 22| **UC-22** | Tra cứu Danh sách Học kỳ | Toàn bộ 4 Actors | Module 3 | Thành Huy | `GET /api/v1/academic/semesters` | `isAuthenticated()` |
| 23| **UC-23** | Thêm mới Phòng học Cơ sở Vật chất | Cán bộ Đào tạo (Staff) | Module 3 | Thành Huy | `POST /api/v1/academic/rooms` | `hasRole('STAFF')` |
| 24| **UC-24** | Cập nhật Sức chứa & Thông tin Phòng học | Cán bộ Đào tạo (Staff) | Module 3 | Thành Huy | `PUT /api/v1/academic/rooms/{id}` | `hasRole('STAFF')` |
| 25| **UC-25** | Tra cứu Danh mục Phòng học | Toàn bộ 4 Actors | Module 3 | Thành Huy | `GET /api/v1/academic/rooms` | `isAuthenticated()` |
| 26| **UC-26** | Mở Lớp Học phần Mới trong Học kỳ | Cán bộ Đào tạo (Staff) | Module 3 | Thành Huy | `POST /api/v1/academic/sections` | `hasRole('STAFF')` |
| 27| **UC-27** | Cập nhật Thông tin Lớp Học phần | Cán bộ Đào tạo (Staff) | Module 3 | Thành Huy | `PUT /api/v1/academic/sections/{id}` | `hasRole('STAFF')` |
| 28| **UC-28** | Phân công Giảng viên Phụ trách Lớp | Cán bộ Đào tạo (Staff) | Module 3 | Thành Huy | `PUT /api/v1/academic/sections/{id}/lecturer` | `hasRole('STAFF')` |
| 29| **UC-29** | Cấu hình Buổi học (Slot) & Check Trùng Lịch | Cán bộ Đào tạo (Staff) | Module 3 | Thành Huy | `POST /api/v1/academic/sections/{id}/slots` | `hasRole('STAFF')` |
| 30| **UC-30** | Gỡ bỏ Buổi học khỏi Lịch của Lớp | Cán bộ Đào tạo (Staff) | Module 3 | Thành Huy | `DELETE /api/v1/academic/sections/{id}/slots/{sId}` | `hasRole('STAFF')` |
| 31| **UC-31** | Điều chỉnh Chỉ tiêu Sức chứa Lớp an toàn | Cán bộ Đào tạo (Staff) | Module 3 | Thành Huy | `PUT /api/v1/academic/sections/{id}/capacity` | `hasRole('STAFF')` |
| 32| **UC-32** | Hủy Lớp Học phần (Cancel Section) | Cán bộ Đào tạo (Staff) | Module 3 | Thành Huy | `PUT /api/v1/academic/sections/{id}/cancel` | `hasRole('STAFF')` |
| **IV**| | **MODULE 4: COURSE REGISTRATION ENGINE** | | **Module 4** | **Quốc An** | | |
| 33| **UC-33** | Thiết lập Đợt Mở Cổng Đăng ký Mới | Cán bộ Đào tạo (Staff) | Module 4 | Quốc An | `POST /api/v1/academic/registration-phases` | `hasRole('STAFF')` |
| 34| **UC-34** | Cập nhật Khung giờ Đợt Đăng ký | Cán bộ Đào tạo (Staff) | Module 4 | Quốc An | `PUT /api/v1/academic/registration-phases/{id}` | `hasRole('STAFF')` |
| 35| **UC-35** | Đóng Khẩn cấp Đợt Đăng ký Học phần | Cán bộ Đào tạo (Staff) | Module 4 | Quốc An | `PUT /api/v1/academic/registration-phases/{id}/close` | `hasRole('STAFF')` |
| 36| **UC-36** | Tra cứu Lớp Học phần Mở Còn Chỗ Trống | Sinh viên (Student), Staff | Module 4 | Quốc An | `GET /api/v1/sections?openOnly=true` | `isAuthenticated()` |
| 37| **UC-37** | Xem Chi tiết Lớp Học phần & Giảng viên/Lịch | Sinh viên (Student), Staff | Module 4 | Quốc An | `GET /api/v1/sections/{id}` | `isAuthenticated()` |
| 38| **UC-38** | **Đăng ký Lớp Học phần (Atomic Transaction)** | Sinh viên (Student) | Module 4 | Quốc An | `POST /api/v1/student/registrations` | `hasRole('STUDENT')` |
| 39| **UC-39** | **Hủy Đăng ký Lớp Học phần (Atomic Drop)** | Sinh viên (Student) | Module 4 | Quốc An | `DELETE /api/v1/student/registrations/{secId}` | `hasRole('STUDENT')` |
| 40| **UC-40** | Xem Danh sách Lớp Đã Đăng ký trong Kỳ | Sinh viên (Student) | Module 4 | Quốc An | `GET /api/v1/student/registrations/active` | `hasRole('STUDENT')` |
| 41| **UC-41** | Xem Ma trận Thời khóa biểu Tuần (T2-CN) | Sinh viên (Student) | Module 4 | Quốc An | `GET /api/v1/student/registrations/active` | `hasRole('STUDENT')` |
| **V** | | **MODULE 5: RECORDS, REPORTING & AUDITING** | | **Module 5** | **Huynh Long** | | |
| 42| **UC-42** | Tra cứu Danh sách Sinh viên Lớp (Class Roster) | Giảng viên, Staff, Admin | Module 5 | Huynh Long | `GET /api/v1/lecturer/sections/{secId}/students` | `hasAnyRole('LECTURER', 'STAFF', 'ADMIN')` |
| 43| **UC-43** | Xuất Danh sách Điểm danh Lớp (PDF/Excel) | Giảng viên (Lecturer), Staff | Module 5 | Huynh Long | `GET /api/v1/lecturer/sections/{secId}/export` | `hasAnyRole('LECTURER', 'STAFF')` |
| 44| **UC-44** | Sinh viên Xem Danh sách Bạn Cùng Lớp | Sinh viên (Student đã học) | Module 5 | Huynh Long | `GET /api/v1/student/registrations/sections/{id}/roster` | `hasRole('STUDENT')` |
| 45| **UC-45** | Dashboard Thống kê Tỷ lệ Lấp đầy Lớp HP | Cán bộ Đào tạo (Staff) | Module 5 | Huynh Long | `GET /api/v1/academic/reports/occupancy` | `hasRole('STAFF')` |
| 46| **UC-46** | Cảnh báo Danh sách Lớp Nguy cơ Bị Hủy (<15) | Cán bộ Đào tạo (Staff) | Module 5 | Huynh Long | `GET /api/v1/academic/reports/occupancy` | `hasRole('STAFF')` |
| 47| **UC-47** | Tra cứu Nhật ký Kiểm toán Đăng ký (Audit Logs) | Admin, Academic Staff | Module 5 | Huynh Long | `GET /api/v1/admin/audit-logs` | `hasAnyRole('ADMIN', 'STAFF')` |
| 48| **UC-48** | Nhập / Cập nhật Điểm Môn học cho Sinh viên | Cán bộ Đào tạo (Staff) | Module 5 | Huynh Long | `POST /api/v1/academic/grades` | `hasRole('STAFF')` |
| 49| **UC-49** | Sinh viên Tra cứu Bảng điểm Tích lũy Cá nhân | Sinh viên (Student) | Module 5 | Huynh Long | `GET /api/v1/academic/grades/my-transcript` | `hasRole('STUDENT')` |
| 50| **UC-50** | Tra cứu Bảng điểm Chi tiết của Sinh viên | Cán bộ Đào tạo (Staff) | Module 5 | Huynh Long | `GET /api/v1/academic/grades/student/{id}` | `hasRole('STAFF')` |
| 51| **UC-51** | Xóa Học kỳ khỏi Hệ thống | Cán bộ Đào tạo (Staff) | Module 3 | Thành Huy | `DELETE /api/v1/academic/semesters/{id}` | `hasRole('STAFF')` |
| 52| **UC-52** | Xóa Phòng học khỏi Hệ thống | Cán bộ Đào tạo (Staff) | Module 3 | Thành Huy | `DELETE /api/v1/academic/rooms/{id}` | `hasRole('STAFF')` |

### 📌 CHI TIẾT NGHIỆP VỤ & RÀNG BUỘC THEO SOURCE CODE (UC-51 & UC-52)

#### 1. UC-51: Xóa Học kỳ khỏi Hệ thống (`DELETE /api/v1/academic/semesters/{id}`)
- **Vị trí code:** 
  - Controller: `com.unienroll.modules.scheduling.controller.AcademicSemesterController.deleteSemester(@PathVariable Long id)`
  - Service: `com.unienroll.modules.scheduling.service.SemesterService.deleteSemester(Long id)`
- **Quyền hạn (Security):** `@PreAuthorize("hasRole('STAFF')")`
- **Ràng buộc kiểm tra (Validation & Business Invariants):**
  - Tìm kiếm học kỳ theo `id`. Nếu không tồn tại: ném lỗi `ERR_NOT_FOUND` ("Không tìm thấy học kỳ với ID: {id}").
  - Kiểm tra liên kết lớp học phần (`sectionRepository.existsBySemesterId(id)`): Nếu học kỳ đã có lớp học phần mở, ném lỗi `ERR_VALIDATION_FAILED` ("Không thể xóa học kỳ đã có lớp học phần mở").
  - Kiểm tra liên kết đợt đăng ký (`phaseRepository.existsBySemesterId(id)`): Nếu học kỳ đã được cấu hình đợt đăng ký, ném lỗi `ERR_VALIDATION_FAILED` ("Không thể xóa học kỳ đã cấu hình đợt đăng ký").
  - Khi thỏa mãn tất cả điều kiện, thực hiện xóa khỏi CSDL (`semesterRepository.delete(semester)`).

#### 2. UC-52: Xóa Phòng học khỏi Hệ thống (`DELETE /api/v1/academic/rooms/{id}`)
- **Vị trí code:**
  - Controller: `com.unienroll.modules.scheduling.controller.AcademicRoomController.deleteRoom(@PathVariable Long id)`
  - Service: `com.unienroll.modules.scheduling.service.RoomService.deleteRoom(Long id)`
- **Quyền hạn (Security):** `@PreAuthorize("hasRole('STAFF')")`
- **Ràng buộc kiểm tra (Validation & Business Invariants):**
  - Tìm kiếm phòng học theo `id`. Nếu không tồn tại: ném lỗi `ERR_NOT_FOUND` ("Không tìm thấy phòng học với ID: {id}").
  - Kiểm tra liên kết lịch học (`scheduleSlotRepository.existsByRoomId(id)`): Nếu phòng học đã được xếp lịch cho buổi học của bất kỳ lớp học phần nào, ném lỗi `ERR_VALIDATION_FAILED` ("Không thể xóa phòng học đã được xếp lịch cho lớp học phần").
  - Khi thỏa mãn tất cả điều kiện, thực hiện xóa khỏi CSDL (`roomRepository.delete(room)`).
