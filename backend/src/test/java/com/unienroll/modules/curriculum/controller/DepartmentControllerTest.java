package com.unienroll.modules.curriculum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.User;
import com.unienroll.modules.auth.entity.UserStatus;
import com.unienroll.modules.auth.repository.UserRepository;
import com.unienroll.modules.curriculum.dto.DepartmentRequest;
import com.unienroll.modules.curriculum.entity.Department;
import com.unienroll.modules.curriculum.repository.DepartmentRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String staffToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAll();
        userRepository.deleteAll();

        // Tạo Staff
        User staff = User.builder()
                .username("staff_curriculum")
                .password(passwordEncoder.encode("Password@123"))
                .email("staff@unienroll.edu.vn")
                .role(Role.ROLE_STAFF)
                .status(UserStatus.ACTIVE)
                .build();
        User savedStaff = userRepository.save(staff);
        staffToken = jwtTokenProvider.generateAccessToken(savedStaff.getId(), savedStaff.getUsername(), savedStaff.getRole());

        // Tạo Student
        User student = User.builder()
                .username("student_curriculum")
                .password(passwordEncoder.encode("Password@123"))
                .email("student@unienroll.edu.vn")
                .role(Role.ROLE_STUDENT)
                .status(UserStatus.ACTIVE)
                .build();
        User savedStudent = userRepository.save(student);
        studentToken = jwtTokenProvider.generateAccessToken(savedStudent.getId(), savedStudent.getUsername(), savedStudent.getRole());
    }

    @Test
    @DisplayName("UC-05: Cán bộ đào tạo (Staff) tạo mới Khoa thành công trả về HTTP 201 Created")
    void shouldCreateDepartmentSuccessfully() throws Exception {
        DepartmentRequest request = DepartmentRequest.builder()
                .code("IT")
                .name("Khoa Công nghệ Thông tin")
                .description("Đào tạo CNTT và Kỹ thuật Phần mềm")
                .build();

        mockMvc.perform(post("/api/v1/admin/departments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("IT"))
                .andExpect(jsonPath("$.data.name").value("Khoa Công nghệ Thông tin"));
    }

    @Test
    @DisplayName("UC-05: Tạo Khoa với mã đã tồn tại trả về HTTP 409 Conflict")
    void shouldReturnConflictWhenDepartmentCodeExists() throws Exception {
        departmentRepository.save(Department.builder().code("IT").name("Khoa CNTT").build());

        DepartmentRequest request = DepartmentRequest.builder()
                .code("IT")
                .name("Khoa Trùng Mã")
                .build();

        mockMvc.perform(post("/api/v1/admin/departments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_DEPARTMENT_CODE_DUPLICATE"));
    }

    @Test
    @DisplayName("UC-05: Sinh viên (Student) không được phép tạo Khoa trả về HTTP 403 Forbidden")
    void shouldRejectStudentFromCreatingDepartment() throws Exception {
        DepartmentRequest request = DepartmentRequest.builder()
                .code("SE")
                .name("Kỹ thuật phần mềm")
                .build();

        mockMvc.perform(post("/api/v1/admin/departments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_FORBIDDEN"));
    }

    @Test
    @DisplayName("UC-05: Lấy danh sách toàn bộ các Khoa thành công")
    void shouldGetAllDepartmentsSuccessfully() throws Exception {
        departmentRepository.save(Department.builder().code("IT").name("Khoa CNTT").build());
        departmentRepository.save(Department.builder().code("BUS").name("Khoa Kinh tế").build());

        mockMvc.perform(get("/api/v1/admin/departments")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }
}
