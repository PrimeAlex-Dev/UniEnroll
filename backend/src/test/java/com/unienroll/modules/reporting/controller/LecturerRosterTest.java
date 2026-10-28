package com.unienroll.modules.reporting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.auth.entity.*;
import com.unienroll.modules.auth.repository.*;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.entity.Department;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.curriculum.repository.DepartmentRepository;
import com.unienroll.modules.engine.entity.EnrollmentRecord;
import com.unienroll.modules.engine.entity.EnrollmentStatus;
import com.unienroll.modules.engine.repository.EnrollmentRecordRepository;
import com.unienroll.modules.engine.repository.RegistrationPhaseRepository;
import com.unienroll.modules.reporting.repository.GradeRecordRepository;
import com.unienroll.modules.reporting.repository.RegistrationAuditLogRepository;
import com.unienroll.modules.scheduling.entity.CourseSection;
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

import java.time.Instant;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LecturerRosterTest {

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

    private String lecturerAToken;
    private String lecturerBToken;
    private String studentToken;
    private String staffToken;

    private CourseSection sectionOfLecturerA;

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

        // 1. Tạo Khoa
        Department dept = departmentRepository.save(Department.builder().code("CS").name("Khoa CNTT").build());

        // 2. Tạo Giảng viên A
        User userLecturerA = userRepository.save(User.builder()
                .username("lecturer_a")
                .password(passwordEncoder.encode("Pass@123"))
                .email("lecturerA@unienroll.edu.vn")
                .role(Role.ROLE_LECTURER)
                .status(UserStatus.ACTIVE)
                .build());
        LecturerProfile profileLecturerA = lecturerRepository.save(LecturerProfile.builder()
                .lecturerCode("LEC_001")
                .fullName("TS. Nguyễn Văn A")
                .departmentName("Khoa CNTT")
                .user(userLecturerA)
                .build());
        lecturerAToken = jwtTokenProvider.generateAccessToken(userLecturerA.getId(), userLecturerA.getUsername(), userLecturerA.getRole());

        // 3. Tạo Giảng viên B
        User userLecturerB = userRepository.save(User.builder()
                .username("lecturer_b")
                .password(passwordEncoder.encode("Pass@123"))
                .email("lecturerB@unienroll.edu.vn")
                .role(Role.ROLE_LECTURER)
                .status(UserStatus.ACTIVE)
                .build());
        LecturerProfile profileLecturerB = lecturerRepository.save(LecturerProfile.builder()
                .lecturerCode("LEC_002")
                .fullName("ThS. Trần Thị B")
                .departmentName("Khoa CNTT")
                .user(userLecturerB)
                .build());
        lecturerBToken = jwtTokenProvider.generateAccessToken(userLecturerB.getId(), userLecturerB.getUsername(), userLecturerB.getRole());

        // 4. Tạo Sinh viên
        User userStudent = userRepository.save(User.builder()
                .username("student_roster")
                .password(passwordEncoder.encode("Pass@123"))
                .email("student@unienroll.edu.vn")
                .role(Role.ROLE_STUDENT)
                .status(UserStatus.ACTIVE)
                .build());
        StudentProfile profileStudent = studentRepository.save(StudentProfile.builder()
                .studentCode("SV_ROSTER_01")
                .fullName("Lê Văn Sinh Viên")
                .cohort("K65")
                .major("Kỹ thuật Phần mềm")
                .user(userStudent)
                .build());
        studentToken = jwtTokenProvider.generateAccessToken(userStudent.getId(), userStudent.getUsername(), userStudent.getRole());

        // 5. Tạo Staff
        User userStaff = userRepository.save(User.builder()
                .username("staff_roster")
                .password(passwordEncoder.encode("Pass@123"))
                .email("staff@unienroll.edu.vn")
                .role(Role.ROLE_STAFF)
                .status(UserStatus.ACTIVE)
                .build());
        staffToken = jwtTokenProvider.generateAccessToken(userStaff.getId(), userStaff.getUsername(), userStaff.getRole());

        // 6. Tạo Học kỳ & Môn học
        Semester semester = semesterRepository.save(Semester.builder()
                .code("2026_HK1")
                .name("Học kỳ 1 Năm 2026")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2027, 1, 15))
                .status(SemesterStatus.ACTIVE)
                .build());

        Course course = courseRepository.save(Course.builder()
                .code("CS101")
                .name("Lập trình Hướng đối tượng")
                .theoryCredits(3)
                .practicalCredits(0)
                .totalCredits(3)
                .department(dept)
                .build());

        // 7. Tạo Lớp học phần do Giảng viên A phụ trách
        sectionOfLecturerA = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS101.N01")
                .course(course)
                .semester(semester)
                .lecturer(profileLecturerA)
                .capacity(40)
                .enrolledCount(1)
                .build());

        // Gán 1 sinh viên vào lớp
        enrollmentRepository.save(EnrollmentRecord.builder()
                .student(profileStudent)
                .section(sectionOfLecturerA)
                .course(course)
                .semester(semester)
                .status(EnrollmentStatus.ENROLLED)
                .enrolledAt(Instant.now())
                .build());
    }

    @Test
    @DisplayName("UC-17: Giảng viên được phân công xem sổ lớp thành công -> 200 OK")
    void shouldAllowAssignedLecturerToViewClassRoster() throws Exception {
        mockMvc.perform(get("/api/v1/lecturer/sections/{sectionId}/students", sectionOfLecturerA.getId())
                        .header("Authorization", "Bearer " + lecturerAToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sectionCode").value("CS101.N01"))
                .andExpect(jsonPath("$.data.courseCode").value("CS101"))
                .andExpect(jsonPath("$.data.lecturerName").value("TS. Nguyễn Văn A"))
                .andExpect(jsonPath("$.data.enrolledCount").value(1))
                .andExpect(jsonPath("$.data.students", hasSize(1)))
                .andExpect(jsonPath("$.data.students[0].studentCode").value("SV_ROSTER_01"))
                .andExpect(jsonPath("$.data.students[0].fullName").value("Lê Văn Sinh Viên"));
    }

    @Test
    @DisplayName("UC-17 (E17.1): Giảng viên B cố tình truy cập lớp của Giảng viên A -> 403 Forbidden")
    void shouldDenyUnauthorizedLecturerFromViewingOtherClassRoster() throws Exception {
        mockMvc.perform(get("/api/v1/lecturer/sections/{sectionId}/students", sectionOfLecturerA.getId())
                        .header("Authorization", "Bearer " + lecturerBToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ERR_ACCESS_DENIED_NOT_ASSIGNED_LECTURER.name()));
    }

    @Test
    @DisplayName("UC-17: Sinh viên cố tình truy cập endpoint giảng viên -> 403 Forbidden (RBAC)")
    void shouldDenyStudentFromAccessingLecturerEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/lecturer/sections/{sectionId}/students", sectionOfLecturerA.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("UC-17: Academic Staff có quyền xem sổ lớp của bất kỳ giảng viên nào -> 200 OK")
    void shouldAllowStaffToViewAnyClassRoster() throws Exception {
        mockMvc.perform(get("/api/v1/lecturer/sections/{sectionId}/students", sectionOfLecturerA.getId())
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.students", hasSize(1)));
    }
}
