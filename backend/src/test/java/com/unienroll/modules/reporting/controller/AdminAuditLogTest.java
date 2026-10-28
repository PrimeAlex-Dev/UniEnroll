package com.unienroll.modules.reporting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.User;
import com.unienroll.modules.auth.entity.UserStatus;
import com.unienroll.modules.auth.repository.UserRepository;
import com.unienroll.modules.reporting.entity.AuditActionType;
import com.unienroll.modules.reporting.entity.AuditExecutionStatus;
import com.unienroll.modules.reporting.entity.RegistrationAuditLog;
import com.unienroll.modules.reporting.repository.RegistrationAuditLogRepository;
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
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminAuditLogTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RegistrationAuditLogRepository auditLogRepository;

    @Autowired
    private com.unienroll.modules.engine.repository.EnrollmentRecordRepository enrollmentRepository;

    @Autowired
    private com.unienroll.modules.auth.repository.StudentProfileRepository studentRepository;

    @Autowired
    private com.unienroll.modules.auth.repository.LecturerProfileRepository lecturerRepository;

    @Autowired
    private com.unienroll.modules.auth.repository.StaffProfileRepository staffRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String staffToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        enrollmentRepository.deleteAll();
        studentRepository.deleteAll();
        lecturerRepository.deleteAll();
        staffRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Tạo Admin
        User admin = userRepository.save(User.builder()
                .username("admin_audit")
                .password(passwordEncoder.encode("Pass@123"))
                .email("admin_audit@unienroll.edu.vn")
                .role(Role.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
        adminToken = jwtTokenProvider.generateAccessToken(admin.getId(), admin.getUsername(), admin.getRole());

        // 2. Tạo Staff
        User staff = userRepository.save(User.builder()
                .username("staff_audit")
                .password(passwordEncoder.encode("Pass@123"))
                .email("staff_audit@unienroll.edu.vn")
                .role(Role.ROLE_STAFF)
                .status(UserStatus.ACTIVE)
                .build());
        staffToken = jwtTokenProvider.generateAccessToken(staff.getId(), staff.getUsername(), staff.getRole());

        // 3. Tạo Student
        User student = userRepository.save(User.builder()
                .username("student_audit")
                .password(passwordEncoder.encode("Pass@123"))
                .email("student_audit@unienroll.edu.vn")
                .role(Role.ROLE_STUDENT)
                .status(UserStatus.ACTIVE)
                .build());
        studentToken = jwtTokenProvider.generateAccessToken(student.getId(), student.getUsername(), student.getRole());

        // 4. Tạo mẫu các bản ghi audit log
        auditLogRepository.save(RegistrationAuditLog.builder()
                .actorId(101L)
                .actorUsername("student_01")
                .actionType(AuditActionType.ENROLL)
                .targetSectionId(1L)
                .targetCourseCode("CS101")
                .timestamp(Instant.now().minus(2, ChronoUnit.HOURS))
                .ipAddress("192.168.1.10")
                .executionStatus(AuditExecutionStatus.SUCCESS)
                .build());

        auditLogRepository.save(RegistrationAuditLog.builder()
                .actorId(102L)
                .actorUsername("student_02")
                .actionType(AuditActionType.DROP)
                .targetSectionId(1L)
                .targetCourseCode("CS101")
                .timestamp(Instant.now().minus(1, ChronoUnit.HOURS))
                .ipAddress("192.168.1.11")
                .executionStatus(AuditExecutionStatus.SUCCESS)
                .build());

        auditLogRepository.save(RegistrationAuditLog.builder()
                .actorId(103L)
                .actorUsername("student_03")
                .actionType(AuditActionType.ENROLL)
                .targetSectionId(2L)
                .targetCourseCode("CS102")
                .timestamp(Instant.now())
                .ipAddress("192.168.1.12")
                .executionStatus(AuditExecutionStatus.FAILED)
                .errorMessage("Lớp học phần đã hết chỗ khả dụng")
                .build());
    }

    @Test
    @DisplayName("UC-19: Admin tra cứu toàn bộ log kiểm toán thành công -> 200 OK")
    void shouldReturnAllAuditLogsForAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    @DisplayName("UC-19: Lọc log theo actionType=ENROLL -> Trả về đúng 2 bản ghi ENROLL")
    void shouldFilterAuditLogsByActionType() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .param("actionType", "ENROLL")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].actionType").value("ENROLL"))
                .andExpect(jsonPath("$.data[1].actionType").value("ENROLL"));
    }

    @Test
    @DisplayName("UC-19: Lọc log theo executionStatus=FAILED -> Trả về bản ghi lỗi")
    void shouldFilterAuditLogsByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .param("status", "FAILED")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].errorMessage").value("Lớp học phần đã hết chỗ khả dụng"));
    }

    @Test
    @DisplayName("UC-19: Sinh viên cố tình truy cập nhật ký kiểm toán -> 403 Forbidden")
    void shouldDenyStudentFromAccessingAuditLogs() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
