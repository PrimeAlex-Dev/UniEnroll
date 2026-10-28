package com.unienroll.modules.curriculum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.User;
import com.unienroll.modules.auth.entity.UserStatus;
import com.unienroll.modules.auth.repository.UserRepository;
import com.unienroll.modules.curriculum.dto.CourseRequest;
import com.unienroll.modules.curriculum.dto.PrerequisiteAssignmentRequest;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.entity.Department;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.curriculum.repository.DepartmentRepository;
import com.unienroll.modules.curriculum.repository.PrerequisiteConditionRepository;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PrerequisiteConditionRepository prerequisiteRepository;

    @Autowired
    private com.unienroll.modules.reporting.repository.GradeRecordRepository gradeRecordRepository;

    @Autowired
    private com.unienroll.modules.engine.repository.EnrollmentRecordRepository enrollmentRecordRepository;

    @Autowired
    private com.unienroll.modules.engine.repository.RegistrationPhaseRepository phaseRepository;

    @Autowired
    private com.unienroll.modules.scheduling.repository.ScheduleSlotRepository scheduleSlotRepository;

    @Autowired
    private com.unienroll.modules.scheduling.repository.CourseSectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String staffToken;
    private Long departmentId;
    private Course savedCourse1;
    private Course savedCourse2;

    @BeforeEach
    void setUp() {
        enrollmentRecordRepository.deleteAll();
        phaseRepository.deleteAll();
        scheduleSlotRepository.deleteAll();
        sectionRepository.deleteAll();
        gradeRecordRepository.deleteAll();
        prerequisiteRepository.deleteAll();
        courseRepository.deleteAll();
        departmentRepository.deleteAll();
        userRepository.deleteAll();

        // Tạo Staff user
        User staff = User.builder()
                .username("staff_course")
                .password(passwordEncoder.encode("Password@123"))
                .email("staff_course@unienroll.edu.vn")
                .role(Role.ROLE_STAFF)
                .status(UserStatus.ACTIVE)
                .build();
        User savedStaff = userRepository.save(staff);
        staffToken = jwtTokenProvider.generateAccessToken(savedStaff.getId(), savedStaff.getUsername(), savedStaff.getRole());

        // Tạo Khoa
        Department dept = departmentRepository.save(Department.builder()
                .code("IT")
                .name("Khoa CNTT")
                .build());
        departmentId = dept.getId();

        // Tạo sẵn 2 môn học
        savedCourse1 = courseRepository.save(Course.builder()
                .code("CS101")
                .name("Nhập môn Lập trình")
                .theoryCredits(2)
                .practicalCredits(1)
                .totalCredits(3)
                .department(dept)
                .build());

        savedCourse2 = courseRepository.save(Course.builder()
                .code("CS102")
                .name("Cấu trúc Dữ liệu")
                .theoryCredits(3)
                .practicalCredits(1)
                .totalCredits(4)
                .department(dept)
                .build());
    }

    @Test
    @DisplayName("UC-06: Tạo mới Môn học thành công trả về HTTP 201 Created")
    void shouldCreateCourseSuccessfully() throws Exception {
        CourseRequest request = CourseRequest.builder()
                .code("CS201")
                .name("Thiết kế Thuật toán")
                .theoryCredits(3)
                .practicalCredits(1)
                .departmentId(departmentId)
                .description("Môn học nâng cao")
                .build();

        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("CS201"))
                .andExpect(jsonPath("$.data.totalCredits").value(4));
    }

    @Test
    @DisplayName("UC-06: Tạo Môn học với mã đã tồn tại trả về HTTP 409 Conflict")
    void shouldReturnConflictWhenCourseCodeExists() throws Exception {
        CourseRequest request = CourseRequest.builder()
                .code("CS101") // Trùng với CS101
                .name("Môn Trùng")
                .theoryCredits(3)
                .practicalCredits(0)
                .departmentId(departmentId)
                .build();

        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_COURSE_CODE_DUPLICATE"));
    }

    @Test
    @DisplayName("UC-06: Tạo Môn học với tổng tín chỉ = 0 trả về HTTP 400 Bad Request")
    void shouldFailWhenTotalCreditsIsZero() throws Exception {
        CourseRequest request = CourseRequest.builder()
                .code("CS999")
                .name("Môn 0 tín chỉ")
                .theoryCredits(0)
                .practicalCredits(0)
                .departmentId(departmentId)
                .build();

        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("UC-07: Gán Môn học tiên quyết hợp lệ thành công (CS102 cần CS101)")
    void shouldAssignPrerequisiteSuccessfully() throws Exception {
        PrerequisiteAssignmentRequest request = PrerequisiteAssignmentRequest.builder()
                .prerequisiteCourseIds(List.of(savedCourse1.getId()))
                .build();

        mockMvc.perform(post("/api/v1/courses/" + savedCourse2.getId() + "/prerequisites")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].code").value("CS101"));
    }

    @Test
    @DisplayName("UC-07: Chặn gán môn học tự làm tiên quyết cho chính mình trả về HTTP 400 ERR_CIRCULAR_PREREQUISITE_DETECTED")
    void shouldRejectSelfPrerequisite() throws Exception {
        PrerequisiteAssignmentRequest request = PrerequisiteAssignmentRequest.builder()
                .prerequisiteCourseIds(List.of(savedCourse1.getId()))
                .build();

        mockMvc.perform(post("/api/v1/courses/" + savedCourse1.getId() + "/prerequisites")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_CIRCULAR_PREREQUISITE_DETECTED"));
    }

    @Test
    @DisplayName("UC-07: Chặn phụ thuộc vòng tròn 2 chiều (CS101 cần CS102 khi CS102 đã cần CS101)")
    void shouldRejectCircularPrerequisite() throws Exception {
        // Bước 1: Gán CS102 cần CS101
        PrerequisiteAssignmentRequest step1 = PrerequisiteAssignmentRequest.builder()
                .prerequisiteCourseIds(List.of(savedCourse1.getId()))
                .build();

        mockMvc.perform(post("/api/v1/courses/" + savedCourse2.getId() + "/prerequisites")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(step1)))
                .andExpect(status().isOk());

        // Bước 2: Thử gán CS101 cần CS102 -> Tạo chu trình vòng tròn
        PrerequisiteAssignmentRequest step2 = PrerequisiteAssignmentRequest.builder()
                .prerequisiteCourseIds(List.of(savedCourse2.getId()))
                .build();

        mockMvc.perform(post("/api/v1/courses/" + savedCourse1.getId() + "/prerequisites")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(step2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_CIRCULAR_PREREQUISITE_DETECTED"));
    }
}
