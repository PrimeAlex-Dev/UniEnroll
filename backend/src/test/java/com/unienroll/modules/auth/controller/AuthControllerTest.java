package com.unienroll.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unienroll.modules.auth.dto.LoginRequest;
import com.unienroll.modules.auth.dto.RefreshTokenRequest;
import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.StudentProfile;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

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

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Tạo tài khoản student active
        User student = User.builder()
                .username("student_test")
                .password(passwordEncoder.encode("Password@123"))
                .email("student_test@unienroll.edu.vn")
                .role(Role.ROLE_STUDENT)
                .status(UserStatus.ACTIVE)
                .build();

        StudentProfile profile = StudentProfile.builder()
                .studentCode("SV2026001")
                .fullName("Nguyễn Văn A")
                .cohort("K65")
                .major("Kỹ thuật Phần mềm")
                .user(student)
                .build();
        student.setStudentProfile(profile);
        userRepository.save(student);

        // Tạo tài khoản bị khóa
        User lockedUser = User.builder()
                .username("locked_user")
                .password(passwordEncoder.encode("Password@123"))
                .email("locked@unienroll.edu.vn")
                .role(Role.ROLE_STUDENT)
                .status(UserStatus.LOCKED)
                .build();
        userRepository.save(lockedUser);
    }

    @Test
    @DisplayName("UC-01: Đăng nhập thành công trả về HTTP 200 kèm Access Token và Refresh Token")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("student_test")
                .password("Password@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.username").value("student_test"))
                .andExpect(jsonPath("$.data.user.role").value("ROLE_STUDENT"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("UC-01: Đăng nhập sai mật khẩu trả về HTTP 401 kèm mã lỗi ERR_AUTH_INVALID_CREDENTIALS")
    void shouldFailLoginWhenPasswordIncorrect() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("student_test")
                .password("WrongPassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_AUTH_INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("UC-01: Đăng nhập tài khoản bị khóa trả về HTTP 403 kèm mã lỗi ERR_AUTH_ACCOUNT_LOCKED")
    void shouldFailLoginWhenAccountLocked() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("locked_user")
                .password("Password@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_AUTH_ACCOUNT_LOCKED"));
    }

    @Test
    @DisplayName("UC-02: Làm mới Access Token thành công bằng Refresh Token hợp lệ")
    void shouldRefreshTokenSuccessfully() throws Exception {
        User user = userRepository.findByUsername("student_test").orElseThrow();
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("UC-03: Lấy thông tin tài khoản hiện hành với Bearer Token hợp lệ")
    void shouldGetCurrentUserProfileSuccessfully() throws Exception {
        User user = userRepository.findByUsername("student_test").orElseThrow();
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("student_test"))
                .andExpect(jsonPath("$.data.role").value("ROLE_STUDENT"))
                .andExpect(jsonPath("$.data.profileDetails.studentCode").value("SV2026001"))
                .andExpect(jsonPath("$.data.profileDetails.fullName").value("Nguyễn Văn A"));
    }

    @Test
    @DisplayName("UC-03: Truy cập /users/me không có token trả về HTTP 401 Unauthorized")
    void shouldReturn401WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_AUTH_UNAUTHORIZED"));
    }
}
