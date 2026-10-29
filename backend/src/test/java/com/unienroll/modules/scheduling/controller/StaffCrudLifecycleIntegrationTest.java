package com.unienroll.modules.scheduling.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.User;
import com.unienroll.modules.auth.entity.UserStatus;
import com.unienroll.modules.auth.repository.UserRepository;
import com.unienroll.modules.curriculum.dto.CourseRequest;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.entity.Department;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.curriculum.repository.DepartmentRepository;
import com.unienroll.modules.engine.dto.RegistrationPhaseRequest;
import com.unienroll.modules.engine.entity.RegistrationPhase;
import com.unienroll.modules.engine.repository.RegistrationPhaseRepository;
import com.unienroll.modules.scheduling.dto.CourseSectionRequest;
import com.unienroll.modules.scheduling.dto.RoomRequest;
import com.unienroll.modules.scheduling.dto.ScheduleSlotRequest;
import com.unienroll.modules.scheduling.dto.SemesterRequest;
import com.unienroll.modules.scheduling.entity.CourseSection;
import com.unienroll.modules.scheduling.entity.Room;
import com.unienroll.modules.scheduling.entity.Semester;
import com.unienroll.modules.scheduling.entity.SemesterStatus;
import com.unienroll.modules.scheduling.repository.CourseSectionRepository;
import com.unienroll.modules.scheduling.repository.RoomRepository;
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

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StaffCrudLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CourseSectionRepository sectionRepository;

    @Autowired
    private ScheduleSlotRepository scheduleSlotRepository;

    @Autowired
    private RegistrationPhaseRepository phaseRepository;

    private String staffToken;

    @BeforeEach
    void setUp() {
        User staff = userRepository.findByUsername("staff_lifecycle_test").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("staff_lifecycle_test")
                        .password(passwordEncoder.encode("password123"))
                        .email("staff_lifecycle_test@unienroll.edu.vn")
                        .role(Role.ROLE_STAFF)
                        .status(UserStatus.ACTIVE)
                        .build())
        );
        staffToken = "Bearer " + tokenProvider.generateAccessToken(staff.getId(), staff.getUsername(), staff.getRole());
    }

    @Test
    @DisplayName("UC-06: Xóa môn học rỗng (chưa mở lớp) phải thành công")
    void testDeleteEmptyCourse_Success() throws Exception {
        Department dept = departmentRepository.save(Department.builder()
                .code("TEST_DEP_1")
                .name("Khoa Test 1")
                .build());

        Course course = courseRepository.save(Course.builder()
                .code("CRS_DEL_1")
                .name("Môn test xóa")
                .theoryCredits(3)
                .practicalCredits(0)
                .totalCredits(3)
                .department(dept)
                .build());

        mockMvc.perform(delete("/api/v1/courses/" + course.getId())
                        .header("Authorization", staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertFalse(courseRepository.existsById(course.getId()));
    }

    @Test
    @DisplayName("UC-08: Cập nhật thông tin học kỳ và trạng thái vòng đời")
    void testUpdateSemesterAndStatus_Success() throws Exception {
        Semester sem = semesterRepository.save(Semester.builder()
                .code("SEM_TEST_1")
                .name("Kỳ 1 năm 2030")
                .startDate(LocalDate.of(2030, 9, 1))
                .endDate(LocalDate.of(2031, 1, 15))
                .status(SemesterStatus.PLANNED)
                .build());

        SemesterRequest updateReq = SemesterRequest.builder()
                .code("SEM_TEST_1_UPDATED")
                .name("Kỳ 1 năm 2030 (Đã sửa)")
                .startDate(LocalDate.of(2030, 9, 5))
                .endDate(LocalDate.of(2031, 1, 20))
                .status(SemesterStatus.ACTIVE)
                .build();

        mockMvc.perform(put("/api/v1/academic/semesters/" + sem.getId())
                        .header("Authorization", staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("SEM_TEST_1_UPDATED"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Chuyển trạng thái sang FINISHED
        mockMvc.perform(patch("/api/v1/academic/semesters/" + sem.getId() + "/status")
                        .header("Authorization", staffToken)
                        .param("status", "FINISHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FINISHED"));
    }

    @Test
    @DisplayName("UC-08: Sửa thông tin phòng học và xóa phòng chưa xếp lịch")
    void testRoomCrud_Success() throws Exception {
        Room room = roomRepository.save(Room.builder()
                .building("TÒA X")
                .roomNumber("X101")
                .capacity(50)
                .build());

        RoomRequest updateReq = RoomRequest.builder()
                .building("TÒA X")
                .roomNumber("X101")
                .capacity(65)
                .build();

        mockMvc.perform(put("/api/v1/academic/rooms/" + room.getId())
                        .header("Authorization", staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.capacity").value(65));

        mockMvc.perform(delete("/api/v1/academic/rooms/" + room.getId())
                        .header("Authorization", staffToken))
                .andExpect(status().isOk());

        assertFalse(roomRepository.existsById(room.getId()));
    }

    @Test
    @DisplayName("UC-12: Đóng cổng đăng ký khẩn cấp (closePhase) thành công")
    void testClosePhaseNow_Success() throws Exception {
        Semester sem = semesterRepository.save(Semester.builder()
                .code("SEM_PHASE_1")
                .name("Kỳ mở cổng")
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(100))
                .status(SemesterStatus.ACTIVE)
                .build());

        RegistrationPhase phase = phaseRepository.save(RegistrationPhase.builder()
                .semester(sem)
                .name("Đợt test đóng khẩn cấp")
                .startTime(Instant.now().minus(2, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(48, ChronoUnit.HOURS))
                .build());

        mockMvc.perform(put("/api/v1/academic/phases/" + phase.getId() + "/close")
                        .header("Authorization", staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        RegistrationPhase updated = phaseRepository.findById(phase.getId()).orElseThrow();
        assertTrue(updated.getEndTime().isBefore(Instant.now().plusSeconds(2)));
    }

    @Test
    @DisplayName("UC-09, UC-10: Thêm slot, xóa slot và xóa lớp rỗng thành công")
    void testSectionAndSlotLifecycle_Success() throws Exception {
        Department dept = departmentRepository.save(Department.builder()
                .code("DEP_SEC_1")
                .name("Khoa Phần mềm")
                .build());

        Course course = courseRepository.save(Course.builder()
                .code("CRS_SEC_1")
                .name("Công nghệ phần mềm")
                .theoryCredits(3)
                .practicalCredits(0)
                .totalCredits(3)
                .department(dept)
                .build());

        Semester sem = semesterRepository.save(Semester.builder()
                .code("SEM_SEC_1")
                .name("Kỳ xếp lớp")
                .startDate(LocalDate.of(2028, 1, 1))
                .endDate(LocalDate.of(2028, 5, 30))
                .status(SemesterStatus.ACTIVE)
                .build());

        Room room = roomRepository.save(Room.builder()
                .building("TÒA S")
                .roomNumber("S301")
                .capacity(60)
                .build());

        CourseSection section = sectionRepository.save(CourseSection.builder()
                .course(course)
                .semester(sem)
                .sectionCode("CRS_SEC_1.01")
                .capacity(50)
                .build());

        // Thêm 1 slot
        ScheduleSlotRequest slotReq = ScheduleSlotRequest.builder()
                .roomId(room.getId())
                .dayOfWeek(DayOfWeek.MONDAY)
                .startPeriod(1)
                .endPeriod(3)
                .build();

        String resJson = mockMvc.perform(post("/api/v1/academic/sections/" + section.getId() + "/slots")
                        .header("Authorization", staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(slotReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scheduleSlots").isArray())
                .andReturn().getResponse().getContentAsString();

        // Xóa slot vừa tạo
        CourseSection refreshedSection = sectionRepository.findById(section.getId()).orElseThrow();
        Long slotId = refreshedSection.getScheduleSlots().get(0).getId();

        mockMvc.perform(delete("/api/v1/academic/sections/" + section.getId() + "/slots/" + slotId)
                        .header("Authorization", staffToken))
                .andExpect(status().isOk());

        // Xóa lớp học phần rỗng
        mockMvc.perform(delete("/api/v1/academic/sections/" + section.getId())
                        .header("Authorization", staffToken))
                .andExpect(status().isOk());

        assertFalse(sectionRepository.existsById(section.getId()));
    }
}
