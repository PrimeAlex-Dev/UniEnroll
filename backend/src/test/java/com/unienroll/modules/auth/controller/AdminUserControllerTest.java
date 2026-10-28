package com.unienroll.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unienroll.modules.auth.dto.CreateUserRequest;
import com.unienroll.modules.auth.dto.UpdateUserStatusRequest;
import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.User;
import com.unienroll.modules.auth.entity.UserStatus;
import com.unienroll.modules.auth.repository.UserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String studentToken;
    private Long createdUserId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Tạo Admin
        User admin = User.builder()
                .username("admin_test")
                .password(passwordEncoder.encode("Admin@123"))
                .email("admin_test@unienroll.edu.vn")
                .role(Role.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        User savedAdmin = userRepository.save(admin);
        adminToken = jwtTokenProvider.generateAccessToken(savedAdmin.getId(), savedAdmin.getUsername(), savedAdmin.getRole());

        // Tạo Student
        User student = User.builder()
                .username("student_normal")
                .password(passwordEncoder.encode("Password@123"))
                .email("student_normal@unienroll.edu.vn")
                .role(Role.ROLE_STUDENT)
                .status(UserStatus.ACTIVE)
                .build();
        User savedStudent = userRepository.save(student);
        studentToken = jwtTokenProvider.generateAccessToken(savedStudent.getId(), savedStudent.getUsername(), savedStudent.getRole());
        createdUserId = savedStudent.getId();
    }

    @Test
    @DisplayName("UC-04: Admin tạo tài khoản sinh viên thành công trả về HTTP 201 Created")
    void shouldCreateStudentUserSuccessfully() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("new_student")
                .password("Password@123")
                .email("new_student@unienroll.edu.vn")
                .role(Role.ROLE_STUDENT)
                .fullName("Trần Thị B")
                .studentCode("SV2026002")
                .cohort("K65")
                .major("Khoa học Máy tính")
                .build();

        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("new_student"))
                .andExpect(jsonPath("$.data.role").value("ROLE_STUDENT"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("UC-04: Non-admin (Student) cố gọi API Admin bị từ chối với HTTP 403 Forbidden")
    void shouldRejectNonAdminAccessToAdminEndpoints() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("hacker_user")
                .password("Password@123")
                .email("hacker@unienroll.edu.vn")
                .role(Role.ROLE_ADMIN)
                .fullName("Hacker")
                .build();

        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_FORBIDDEN"));
    }

    @Test
    @DisplayName("UC-04: Tạo tài khoản với username đã tồn tại trả về HTTP 409 Conflict")
    void shouldReturnConflictWhenUsernameAlreadyExists() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("student_normal") // Trùng với user đã có
                .password("Password@123")
                .email("another_email@unienroll.edu.vn")
                .role(Role.ROLE_STUDENT)
                .fullName("Trùng Tên")
                .build();

        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_USER_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("UC-04: Admin cập nhật trạng thái người dùng (LOCKED) thành công")
    void shouldUpdateUserStatusSuccessfully() throws Exception {
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.LOCKED)
                .build();

        mockMvc.perform(put("/api/v1/admin/users/" + createdUserId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(createdUserId))
                .andExpect(jsonPath("$.data.status").value("LOCKED"));
    }
}
