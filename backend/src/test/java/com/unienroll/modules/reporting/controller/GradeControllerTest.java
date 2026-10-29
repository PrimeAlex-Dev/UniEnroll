package com.unienroll.modules.reporting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.StudentProfile;
import com.unienroll.modules.auth.entity.User;
import com.unienroll.modules.auth.entity.UserStatus;
import com.unienroll.modules.auth.repository.StudentProfileRepository;
import com.unienroll.modules.auth.repository.UserRepository;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.entity.Department;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.curriculum.repository.DepartmentRepository;
import com.unienroll.modules.reporting.dto.GradeRecordRequest;
import com.unienroll.modules.reporting.entity.GradeStatus;
import com.unienroll.modules.reporting.repository.GradeRecordRepository;
import com.unienroll.modules.scheduling.entity.Semester;
import com.unienroll.modules.scheduling.entity.SemesterStatus;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GradeRecordRepository gradeRecordRepository;

    @Autowired
    private com.unienroll.modules.engine.repository.EnrollmentRecordRepository enrollmentRecordRepository;

    @Autowired
    private com.unienroll.modules.engine.repository.RegistrationPhaseRepository phaseRepository;

    @Autowired
    private com.unienroll.modules.scheduling.repository.ScheduleSlotRepository scheduleSlotRepository;

    @Autowired
    private com.unienroll.modules.scheduling.repository.CourseSectionRepository sectionRepository;

    @Autowired
    private StudentProfileRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String staffToken;
    private String studentToken;
    private StudentProfile savedStudent;
    private Course savedCourse;
    private Semester savedSemester;

    @BeforeEach
    void setUp() {
        enrollmentRecordRepository.deleteAll();
        scheduleSlotRepository.deleteAll();
        sectionRepository.deleteAll();
        phaseRepository.deleteAll();
        gradeRecordRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();
        departmentRepository.deleteAll();
        semesterRepository.deleteAll();
        userRepository.deleteAll();

        // Tạo Staff
        User staff = User.builder()
                .username("staff_grade")
                .password(passwordEncoder.encode("Password@123"))
                .email("staff_grade@unienroll.edu.vn")
                .role(Role.ROLE_STAFF)
                .status(UserStatus.ACTIVE)
                .build();
        User savedStaff = userRepository.save(staff);
        staffToken = jwtTokenProvider.generateAccessToken(savedStaff.getId(), savedStaff.getUsername(), savedStaff.getRole());

        // Tạo Student
        User studentUser = User.builder()
                .username("student_grade")
                .password(passwordEncoder.encode("Password@123"))
                .email("student_grade@unienroll.edu.vn")
                .role(Role.ROLE_STUDENT)
                .status(UserStatus.ACTIVE)
                .build();
        User savedStuUser = userRepository.save(studentUser);
        studentToken = jwtTokenProvider.generateAccessToken(savedStuUser.getId(), savedStuUser.getUsername(), savedStuUser.getRole());

        savedStudent = studentRepository.save(StudentProfile.builder()
                .studentCode("SV2026009")
                .fullName("Trương Vô Kỵ")
                .cohort("K65")
                .user(savedStuUser)
                .build());

        // Tạo Khoa và Môn học
        Department dept = departmentRepository.save(Department.builder()
                .code("IT")
                .name("Khoa CNTT")
                .build());

        savedCourse = courseRepository.save(Course.builder()
                .code("CS101")
                .name("Lập trình C")
                .theoryCredits(3)
                .practicalCredits(0)
                .totalCredits(3)
                .department(dept)
                .build());

        // Tạo Học kỳ
        savedSemester = semesterRepository.save(Semester.builder()
                .code("2025_2")
                .name("Học kỳ 2 năm 2025-2026")
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .status(SemesterStatus.FINISHED)
                .build());
    }

    @Test
    @DisplayName("Cán bộ Đào tạo (Staff) nhập điểm cho sinh viên thành công trả về HTTP 201 Created")
    void shouldRecordGradeSuccessfully() throws Exception {
        GradeRecordRequest request = GradeRecordRequest.builder()
                .studentId(savedStudent.getId())
                .courseId(savedCourse.getId())
                .semesterId(savedSemester.getId())
                .finalScore(8.5)
                .status(GradeStatus.PASSED)
                .build();

        mockMvc.perform(post("/api/v1/academic/grades")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.finalScore").value(8.5))
                .andExpect(jsonPath("$.data.status").value("PASSED"))
                .andExpect(jsonPath("$.data.studentCode").value("SV2026009"))
                .andExpect(jsonPath("$.data.courseCode").value("CS101"));
    }

    @Test
    @DisplayName("Sinh viên (Student) không được phép tự nhập điểm trả về HTTP 403 Forbidden")
    void shouldRejectStudentFromRecordingGrade() throws Exception {
        GradeRecordRequest request = GradeRecordRequest.builder()
                .studentId(savedStudent.getId())
                .courseId(savedCourse.getId())
                .semesterId(savedSemester.getId())
                .finalScore(10.0)
                .status(GradeStatus.PASSED)
                .build();

        mockMvc.perform(post("/api/v1/academic/grades")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_FORBIDDEN"));
    }

    @Test
    @DisplayName("Tra cứu bảng điểm tích lũy của sinh viên thành công")
    void shouldGetStudentTranscriptSuccessfully() throws Exception {
        // Nhập điểm trước
        GradeRecordRequest request = GradeRecordRequest.builder()
                .studentId(savedStudent.getId())
                .courseId(savedCourse.getId())
                .semesterId(savedSemester.getId())
                .finalScore(9.0)
                .status(GradeStatus.PASSED)
                .build();

        mockMvc.perform(post("/api/v1/academic/grades")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Tra cứu
        mockMvc.perform(get("/api/v1/academic/grades/student/" + savedStudent.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].finalScore").value(9.0))
                .andExpect(jsonPath("$.data[0].status").value("PASSED"));
    }

    @Test
    @DisplayName("Sinh viên tra cứu bảng điểm cá nhân qua endpoint /api/v1/student/grades/my-transcript thành công")
    void shouldGetMyTranscriptSuccessfully() throws Exception {
        // Nhập điểm cho sinh viên
        GradeRecordRequest request = GradeRecordRequest.builder()
                .studentId(savedStudent.getId())
                .courseId(savedCourse.getId())
                .semesterId(savedSemester.getId())
                .finalScore(8.5)
                .status(GradeStatus.PASSED)
                .build();

        mockMvc.perform(post("/api/v1/academic/grades")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Sinh viên tự gọi /api/v1/student/grades/my-transcript
        mockMvc.perform(get("/api/v1/student/grades/my-transcript")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].studentCode").value(savedStudent.getStudentCode()))
                .andExpect(jsonPath("$.data[0].finalScore").value(8.5))
                .andExpect(jsonPath("$.data[0].courseCode").value(savedCourse.getCode()));
    }
}
