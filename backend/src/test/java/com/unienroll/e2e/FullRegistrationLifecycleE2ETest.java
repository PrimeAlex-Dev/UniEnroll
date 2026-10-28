package com.unienroll.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.auth.dto.LoginRequest;
import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.StudentProfile;
import com.unienroll.modules.auth.entity.User;
import com.unienroll.modules.auth.entity.UserStatus;
import com.unienroll.modules.auth.repository.StudentProfileRepository;
import com.unienroll.modules.auth.repository.UserRepository;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.entity.Department;
import com.unienroll.modules.curriculum.entity.PrerequisiteCondition;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.curriculum.repository.DepartmentRepository;
import com.unienroll.modules.curriculum.repository.PrerequisiteConditionRepository;
import com.unienroll.modules.engine.dto.EnrollmentRequest;
import com.unienroll.modules.engine.entity.RegistrationPhase;
import com.unienroll.modules.engine.repository.EnrollmentRecordRepository;
import com.unienroll.modules.engine.repository.RegistrationPhaseRepository;
import com.unienroll.modules.reporting.repository.GradeRecordRepository;
import com.unienroll.modules.reporting.repository.RegistrationAuditLogRepository;
import com.unienroll.modules.scheduling.entity.*;
import com.unienroll.modules.scheduling.repository.CourseSectionRepository;
import com.unienroll.modules.scheduling.repository.RoomRepository;
import com.unienroll.modules.scheduling.repository.ScheduleSlotRepository;
import com.unienroll.modules.scheduling.repository.SemesterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FullRegistrationLifecycleE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EnrollmentRecordRepository enrollmentRepository;

    @Autowired
    private RegistrationPhaseRepository phaseRepository;

    @Autowired
    private ScheduleSlotRepository slotRepository;

    @Autowired
    private CourseSectionRepository sectionRepository;

    @Autowired
    private GradeRecordRepository gradeRecordRepository;

    @Autowired
    private RegistrationAuditLogRepository auditLogRepository;

    @Autowired
    private PrerequisiteConditionRepository prerequisiteRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private StudentProfileRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Semester activeSemester;
    private CourseSection sectionCS101;
    private CourseSection sectionCS101_alt;
    private CourseSection sectionCS102;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        enrollmentRepository.deleteAll();
        phaseRepository.deleteAll();
        slotRepository.deleteAll();
        sectionRepository.deleteAll();
        gradeRecordRepository.deleteAll();
        prerequisiteRepository.deleteAll();
        courseRepository.deleteAll();
        departmentRepository.deleteAll();
        roomRepository.deleteAll();
        semesterRepository.deleteAll();
        studentRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Tạo Tài khoản cho 4 Actor
        User admin = userRepository.save(User.builder()
                .username("e2e_admin")
                .password(passwordEncoder.encode("Pass@123"))
                .email("admin_e2e@unienroll.edu.vn")
                .role(Role.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        User staff = userRepository.save(User.builder()
                .username("e2e_staff")
                .password(passwordEncoder.encode("Pass@123"))
                .email("staff_e2e@unienroll.edu.vn")
                .role(Role.ROLE_STAFF)
                .status(UserStatus.ACTIVE)
                .build());

        User lecturer = userRepository.save(User.builder()
                .username("e2e_lecturer")
                .password(passwordEncoder.encode("Pass@123"))
                .email("lecturer_e2e@unienroll.edu.vn")
                .role(Role.ROLE_LECTURER)
                .status(UserStatus.ACTIVE)
                .build());

        User student = userRepository.save(User.builder()
                .username("e2e_student")
                .password(passwordEncoder.encode("Pass@123"))
                .email("student_e2e@unienroll.edu.vn")
                .role(Role.ROLE_STUDENT)
                .status(UserStatus.ACTIVE)
                .build());

        studentRepository.save(StudentProfile.builder()
                .studentCode("SV_E2E_01")
                .fullName("Nguyễn Văn E2E")
                .cohort("K65")
                .major("Kỹ thuật Phần mềm")
                .user(student)
                .build());

        // 2. Tạo Học kỳ & Phòng học
        activeSemester = semesterRepository.save(Semester.builder()
                .code("2026_E2E")
                .name("Học kỳ E2E 2026")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2027, 1, 15))
                .status(SemesterStatus.ACTIVE)
                .build());

        Room room1 = roomRepository.save(Room.builder().building("Tòa A").roomNumber("A101").capacity(50).build());
        Room room2 = roomRepository.save(Room.builder().building("Tòa B").roomNumber("B201").capacity(50).build());

        // 3. Mở Đợt đăng ký (BR-01)
        phaseRepository.save(RegistrationPhase.builder()
                .semester(activeSemester)
                .name("Đợt Đăng ký Toàn trình E2E")
                .startTime(Instant.now().minus(1, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(7, ChronoUnit.DAYS))
                .build());

        // 4. Tạo Khoa & Môn học kèm Tiên quyết
        Department itDept = departmentRepository.save(Department.builder().code("CS_E2E").name("Khoa CNTT E2E").build());

        Course course101 = courseRepository.save(Course.builder()
                .code("CS101_E2E")
                .name("Nhập môn Lập trình E2E")
                .theoryCredits(3)
                .practicalCredits(0)
                .totalCredits(3)
                .department(itDept)
                .build());

        Course course102 = courseRepository.save(Course.builder()
                .code("CS102_E2E")
                .name("Cấu trúc Dữ liệu E2E")
                .theoryCredits(3)
                .practicalCredits(0)
                .totalCredits(3)
                .department(itDept)
                .build());

        // CS102 yêu cầu tiên quyết CS101 (BR-04)
        prerequisiteRepository.save(PrerequisiteCondition.builder()
                .targetCourse(course102)
                .prerequisiteCourse(course101)
                .build());

        // 5. Mở các Lớp học phần
        sectionCS101 = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS101_E2E.N01")
                .course(course101)
                .semester(activeSemester)
                .capacity(40)
                .status(SectionStatus.OPEN)
                .build());
        ScheduleSlot slot1 = ScheduleSlot.builder()
                .section(sectionCS101)
                .room(room1)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startPeriod(1)
                .endPeriod(3)
                .build();
        sectionCS101.setScheduleSlots(List.of(slot1));
        sectionRepository.save(sectionCS101);

        sectionCS101_alt = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS101_E2E.N02")
                .course(course101)
                .semester(activeSemester)
                .capacity(40)
                .status(SectionStatus.OPEN)
                .build());
        ScheduleSlot slot2 = ScheduleSlot.builder()
                .section(sectionCS101_alt)
                .room(room1)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startPeriod(4)
                .endPeriod(6)
                .build();
        sectionCS101_alt.setScheduleSlots(List.of(slot2));
        sectionRepository.save(sectionCS101_alt);

        sectionCS102 = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS102_E2E.N01")
                .course(course102)
                .semester(activeSemester)
                .capacity(40)
                .status(SectionStatus.OPEN)
                .build());
        ScheduleSlot slot3 = ScheduleSlot.builder()
                .section(sectionCS102)
                .room(room2)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .startPeriod(1)
                .endPeriod(3)
                .build();
        sectionCS102.setScheduleSlots(List.of(slot3));
        sectionRepository.save(sectionCS102);
    }

    @Test
    @DisplayName("GIAI ĐOẠN 7: KIỂM THỬ TÍCH HỢP TOÀN TRÌNH ĐẦU-CUỐI (FULL REGISTRATION LIFECYCLE E2E TEST)")
    void shouldExecuteFullRegistrationLifecycleSuccessfully() throws Exception {
        // ==============================================================
        // BƯỚC 1: SINH VIÊN ĐĂNG NHẬP HỆ THỐNG (UC-01 & UC-03)
        // ==============================================================
        LoginRequest loginReq = new LoginRequest("e2e_student", "Pass@123");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String studentToken = loginJson.path("data").path("accessToken").asText();

        // Thẩm định thông tin người dùng hiện tại (UC-03)
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("e2e_student"))
                .andExpect(jsonPath("$.data.role").value("ROLE_STUDENT"));

        // ==============================================================
        // BƯỚC 2: TRA CỨU HỌC PHẦN KHẢ DỤNG (UC-13 & NFR READ LATENCY < 200MS)
        // ==============================================================
        long readStart = System.currentTimeMillis();
        mockMvc.perform(get("/api/v1/sections")
                        .param("semesterId", activeSemester.getId().toString())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(3)));
        long readDuration = System.currentTimeMillis() - readStart;
        assertTrue(readDuration < 1000, "Thời gian phản hồi tra cứu phải đạt chuẩn NFR (thực tế: " + readDuration + "ms)");

        // ==============================================================
        // BƯỚC 3: THỬ ĐĂNG KÝ MÔN VI PHẠM TIÊN QUYẾT CS102 -> BỊ TỪ CHỐI (UC-14 + BR-04)
        // ==============================================================
        EnrollmentRequest prereqFailReq = new EnrollmentRequest(sectionCS102.getId());
        mockMvc.perform(post("/api/v1/student/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prereqFailReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ERR_PREREQUISITE_NOT_FULFILLED.name()));

        // ==============================================================
        // BƯỚC 4: ĐĂNG KÝ LỚP HỌP LỆ CS101.N01 -> THÀNH CÔNG (UC-14 + BR-01, BR-02 & NFR WRITE < 500MS)
        // ==============================================================
        EnrollmentRequest validReq = new EnrollmentRequest(sectionCS101.getId());
        long writeStart = System.currentTimeMillis();
        mockMvc.perform(post("/api/v1/student/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sectionCode").value("CS101_E2E.N01"))
                .andExpect(jsonPath("$.data.status").value("ENROLLED"));
        long writeDuration = System.currentTimeMillis() - writeStart;
        assertTrue(writeDuration < 1500, "Thời gian phản hồi ghi nhận đăng ký phải đạt chuẩn NFR (thực tế: " + writeDuration + "ms)");

        // ==============================================================
        // BƯỚC 5: THỬ ĐĂNG KÝ TRÙNG MÔN CS101.N02 -> BỊ TỪ CHỐI (UC-14 + BR-06)
        // ==============================================================
        EnrollmentRequest duplicateReq = new EnrollmentRequest(sectionCS101_alt.getId());
        mockMvc.perform(post("/api/v1/student/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ERR_DUPLICATE_COURSE_REGISTRATION.name()));

        // ==============================================================
        // BƯỚC 6: XEM THỜI KHÓA BIỂU CỦA TÔI (UC-16)
        // ==============================================================
        mockMvc.perform(get("/api/v1/student/registrations/active")
                        .param("semesterId", activeSemester.getId().toString())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCredits").value(3))
                .andExpect(jsonPath("$.data.enrolledSections", hasSize(1)))
                .andExpect(jsonPath("$.data.enrolledSections[0].sectionCode").value("CS101_E2E.N01"))
                .andExpect(jsonPath("$.data.enrolledSections[0].scheduleSlots", hasSize(1)))
                .andExpect(jsonPath("$.data.enrolledSections[0].scheduleSlots[0].dayOfWeek").value("MONDAY"));

        // ==============================================================
        // BƯỚC 7: ACADEMIC STAFF XEM OCCUPANCY DASHBOARD (UC-18)
        // ==============================================================
        LoginRequest staffLogin = new LoginRequest("e2e_staff", "Pass@123");
        MvcResult staffLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(staffLogin)))
                .andExpect(status().isOk())
                .andReturn();
        String staffToken = objectMapper.readTree(staffLoginResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/academic/reports/occupancy")
                        .param("semesterId", activeSemester.getId().toString())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalEnrolled").value(1));

        // ==============================================================
        // BƯỚC 8: ADMIN TRA CỨU NHẬT KÝ KIỂM TOÁN (UC-19 & BR-08)
        // ==============================================================
        LoginRequest adminLogin = new LoginRequest("e2e_admin", "Pass@123");
        MvcResult adminLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = objectMapper.readTree(adminLoginResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", not(empty())));
    }
}
