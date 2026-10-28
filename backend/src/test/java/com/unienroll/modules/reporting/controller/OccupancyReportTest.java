package com.unienroll.modules.reporting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.User;
import com.unienroll.modules.auth.entity.UserStatus;
import com.unienroll.modules.auth.repository.LecturerProfileRepository;
import com.unienroll.modules.auth.repository.StudentProfileRepository;
import com.unienroll.modules.auth.repository.UserRepository;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.entity.Department;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.curriculum.repository.DepartmentRepository;
import com.unienroll.modules.engine.repository.EnrollmentRecordRepository;
import com.unienroll.modules.engine.repository.RegistrationPhaseRepository;
import com.unienroll.modules.reporting.repository.GradeRecordRepository;
import com.unienroll.modules.reporting.repository.RegistrationAuditLogRepository;
import com.unienroll.modules.scheduling.entity.CourseSection;
import com.unienroll.modules.scheduling.entity.SectionStatus;
import com.unienroll.modules.scheduling.entity.Semester;
import com.unienroll.modules.scheduling.entity.SemesterStatus;
import com.unienroll.modules.scheduling.repository.CourseSectionRepository;
import com.unienroll.modules.scheduling.repository.ScheduleSlotRepository;
import com.unienroll.modules.scheduling.repository.SemesterRepository;
import com.unienroll.security.JwtTokenProvider;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OccupancyReportTest {

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
    private CourseRepository courseRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private StudentProfileRepository studentRepository;

    @Autowired
    private LecturerProfileRepository lecturerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String staffToken;
    private String studentToken;
    private Semester activeSemester;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        enrollmentRepository.deleteAll();
        phaseRepository.deleteAll();
        slotRepository.deleteAll();
        sectionRepository.deleteAll();
        gradeRecordRepository.deleteAll();
        courseRepository.deleteAll();
        departmentRepository.deleteAll();
        semesterRepository.deleteAll();
        studentRepository.deleteAll();
        lecturerRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Tạo Staff
        User staff = userRepository.save(User.builder()
                .username("staff_report")
                .password(passwordEncoder.encode("Pass@123"))
                .email("staff_report@unienroll.edu.vn")
                .role(Role.ROLE_STAFF)
                .status(UserStatus.ACTIVE)
                .build());
        staffToken = jwtTokenProvider.generateAccessToken(staff.getId(), staff.getUsername(), staff.getRole());

        // 2. Tạo Sinh viên
        User student = userRepository.save(User.builder()
                .username("student_report")
                .password(passwordEncoder.encode("Pass@123"))
                .email("student_report@unienroll.edu.vn")
                .role(Role.ROLE_STUDENT)
                .status(UserStatus.ACTIVE)
                .build());
        studentToken = jwtTokenProvider.generateAccessToken(student.getId(), student.getUsername(), student.getRole());

        // 3. Tạo Khoa & Môn học
        Department dept = departmentRepository.save(Department.builder().code("MATH").name("Khoa Toán").build());
        Course course = courseRepository.save(Course.builder()
                .code("MTH101")
                .name("Giải tích 1")
                .theoryCredits(3)
                .practicalCredits(0)
                .totalCredits(3)
                .department(dept)
                .build());

        // 4. Tạo Học kỳ
        activeSemester = semesterRepository.save(Semester.builder()
                .code("2026_OCCUPANCY")
                .name("Học kỳ Thống kê Lấp đầy")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2027, 1, 15))
                .status(SemesterStatus.ACTIVE)
                .build());

        // 5. Tạo 3 lớp đại diện 3 nhóm:
        // Lớp 1: Đầy 100% (30/30)
        sectionRepository.save(CourseSection.builder()
                .sectionCode("MTH101.FULL")
                .course(course)
                .semester(activeSemester)
                .capacity(30)
                .enrolledCount(30)
                .status(SectionStatus.CLOSED)
                .build());

        // Lớp 2: Ổn định (20/30, tức 66.67%, >= 15 sinh viên)
        sectionRepository.save(CourseSection.builder()
                .sectionCode("MTH101.OPTIMAL")
                .course(course)
                .semester(activeSemester)
                .capacity(30)
                .enrolledCount(20)
                .status(SectionStatus.OPEN)
                .build());

        // Lớp 3: Nguy cơ hủy (< 15 sinh viên, ví dụ 5/30)
        sectionRepository.save(CourseSection.builder()
                .sectionCode("MTH101.AT_RISK")
                .course(course)
                .semester(activeSemester)
                .capacity(30)
                .enrolledCount(5)
                .status(SectionStatus.OPEN)
                .build());
    }

    @Test
    @DisplayName("UC-18: Academic Staff xem Báo cáo Dashboard Tỷ lệ Lấp đầy -> 200 OK & Phân nhóm chính xác")
    void shouldReturnOccupancyDashboardWithCorrectGrouping() throws Exception {
        mockMvc.perform(get("/api/v1/academic/reports/occupancy")
                        .param("semesterId", activeSemester.getId().toString())
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.semesterName").value("Học kỳ Thống kê Lấp đầy"))
                .andExpect(jsonPath("$.data.totalSections").value(3))
                .andExpect(jsonPath("$.data.totalCapacity").value(90))
                .andExpect(jsonPath("$.data.totalEnrolled").value(55))
                // 55 / 90 * 100% = 61.11%
                .andExpect(jsonPath("$.data.overallOccupancyRate").value(61.11))
                // Phân nhóm
                .andExpect(jsonPath("$.data.fullSections", hasSize(1)))
                .andExpect(jsonPath("$.data.fullSections[0].sectionCode").value("MTH101.FULL"))
                .andExpect(jsonPath("$.data.fullSections[0].occupancyRate").value(100.0))
                .andExpect(jsonPath("$.data.optimalSections", hasSize(1)))
                .andExpect(jsonPath("$.data.optimalSections[0].sectionCode").value("MTH101.OPTIMAL"))
                .andExpect(jsonPath("$.data.atRiskSections", hasSize(1)))
                .andExpect(jsonPath("$.data.atRiskSections[0].sectionCode").value("MTH101.AT_RISK"));
    }

    @Test
    @DisplayName("UC-18: Sinh viên cố tình xem Dashboard báo cáo -> 403 Forbidden (RBAC)")
    void shouldDenyStudentFromViewingOccupancyDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/academic/reports/occupancy")
                        .param("semesterId", activeSemester.getId().toString())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("UC-18: Xem báo cáo với học kỳ không tồn tại -> 404 Not Found")
    void shouldReturnNotFoundWhenSemesterDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/academic/reports/occupancy")
                        .param("semesterId", "999999")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ERR_NOT_FOUND.name()));
    }
}
