package com.unienroll.modules.scheduling.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unienroll.modules.auth.entity.LecturerProfile;
import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.User;
import com.unienroll.modules.auth.entity.UserStatus;
import com.unienroll.modules.auth.repository.LecturerProfileRepository;
import com.unienroll.modules.auth.repository.UserRepository;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.entity.Department;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.curriculum.repository.DepartmentRepository;
import com.unienroll.modules.scheduling.dto.*;
import com.unienroll.modules.scheduling.entity.*;
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
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AcademicSectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LecturerProfileRepository lecturerRepository;

    @Autowired
    private CourseSectionRepository sectionRepository;

    @Autowired
    private ScheduleSlotRepository scheduleSlotRepository;

    @Autowired
    private com.unienroll.modules.reporting.repository.GradeRecordRepository gradeRecordRepository;

    @Autowired
    private com.unienroll.modules.engine.repository.EnrollmentRecordRepository enrollmentRecordRepository;

    @Autowired
    private com.unienroll.modules.engine.repository.RegistrationPhaseRepository phaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String staffToken;
    private Semester savedSemester;
    private Room savedRoomSmall;
    private Room savedRoomLarge;
    private Course savedCourse;
    private LecturerProfile savedLecturer;

    @BeforeEach
    void setUp() {
        enrollmentRecordRepository.deleteAll();
        phaseRepository.deleteAll();
        gradeRecordRepository.deleteAll();
        scheduleSlotRepository.deleteAll();
        sectionRepository.deleteAll();
        roomRepository.deleteAll();
        semesterRepository.deleteAll();
        courseRepository.deleteAll();
        departmentRepository.deleteAll();
        lecturerRepository.deleteAll();
        userRepository.deleteAll();

        // Tạo Staff User
        User staff = User.builder()
                .username("staff_scheduler")
                .password(passwordEncoder.encode("Password@123"))
                .email("scheduler@unienroll.edu.vn")
                .role(Role.ROLE_STAFF)
                .status(UserStatus.ACTIVE)
                .build();
        User savedStaff = userRepository.save(staff);
        staffToken = jwtTokenProvider.generateAccessToken(savedStaff.getId(), savedStaff.getUsername(), savedStaff.getRole());

        // Tạo Giảng viên
        User lecturerUser = User.builder()
                .username("lecturer_test")
                .password(passwordEncoder.encode("Password@123"))
                .email("lecturer@unienroll.edu.vn")
                .role(Role.ROLE_LECTURER)
                .status(UserStatus.ACTIVE)
                .build();
        User savedLecUser = userRepository.save(lecturerUser);

        savedLecturer = lecturerRepository.save(LecturerProfile.builder()
                .lecturerCode("LEC001")
                .fullName("TS. Nguyễn Văn Thầy")
                .user(savedLecUser)
                .build());

        // Tạo Học kỳ
        savedSemester = semesterRepository.save(Semester.builder()
                .code("2026_1")
                .name("Học kỳ 1 năm học 2026-2027")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2027, 1, 15))
                .status(SemesterStatus.ACTIVE)
                .build());

        // Tạo Phòng học (1 phòng nhỏ 30 chỗ, 1 phòng lớn 60 chỗ)
        savedRoomSmall = roomRepository.save(Room.builder()
                .building("Tòa A")
                .roomNumber("A101")
                .capacity(30)
                .build());

        savedRoomLarge = roomRepository.save(Room.builder()
                .building("Tòa B")
                .roomNumber("B201")
                .capacity(60)
                .build());

        // Tạo Khoa và Môn học
        Department dept = departmentRepository.save(Department.builder()
                .code("IT")
                .name("Khoa CNTT")
                .build());

        savedCourse = courseRepository.save(Course.builder()
                .code("CS101")
                .name("Lập trình Java")
                .theoryCredits(3)
                .practicalCredits(1)
                .totalCredits(4)
                .department(dept)
                .build());
    }

    @Test
    @DisplayName("UC-08: Cán bộ Đào tạo khởi tạo Học kỳ mới thành công trả về HTTP 201 Created")
    void shouldCreateSemesterSuccessfully() throws Exception {
        SemesterRequest request = SemesterRequest.builder()
                .code("2026_2")
                .name("Học kỳ 2 năm học 2026-2027")
                .startDate(LocalDate.of(2027, 2, 1))
                .endDate(LocalDate.of(2027, 6, 30))
                .status(SemesterStatus.PLANNED)
                .build();

        mockMvc.perform(post("/api/v1/academic/semesters")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("2026_2"));
    }

    @Test
    @DisplayName("UC-08: Tạo Học kỳ với ngày bắt đầu sau ngày kết thúc trả về HTTP 400")
    void shouldFailSemesterWhenStartDateAfterEndDate() throws Exception {
        SemesterRequest request = SemesterRequest.builder()
                .code("2026_INVALID")
                .name("Học kỳ lỗi ngày")
                .startDate(LocalDate.of(2027, 6, 30))
                .endDate(LocalDate.of(2027, 2, 1))
                .build();

        mockMvc.perform(post("/api/v1/academic/semesters")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("UC-09: Mở Lớp Học phần Mới thành công với Nenrolled=0 và status=OPEN")
    void shouldOpenCourseSectionSuccessfully() throws Exception {
        CourseSectionRequest request = CourseSectionRequest.builder()
                .sectionCode("CS101.N01")
                .courseId(savedCourse.getId())
                .semesterId(savedSemester.getId())
                .lecturerId(savedLecturer.getId())
                .capacity(50)
                .build();

        mockMvc.perform(post("/api/v1/academic/sections")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sectionCode").value("CS101.N01"))
                .andExpect(jsonPath("$.data.capacity").value(50))
                .andExpect(jsonPath("$.data.enrolledCount").value(0))
                .andExpect(jsonPath("$.data.availableSlots").value(50))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    @DisplayName("UC-09: Mở lớp học phần trùng mã trong cùng học kỳ trả về HTTP 409 ERR_SECTION_CODE_DUPLICATE")
    void shouldFailWhenDuplicateSectionCodeInSameSemester() throws Exception {
        sectionRepository.save(CourseSection.builder()
                .sectionCode("CS101.N01")
                .course(savedCourse)
                .semester(savedSemester)
                .capacity(50)
                .build());

        CourseSectionRequest request = CourseSectionRequest.builder()
                .sectionCode("CS101.N01")
                .courseId(savedCourse.getId())
                .semesterId(savedSemester.getId())
                .capacity(50)
                .build();

        mockMvc.perform(post("/api/v1/academic/sections")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_SECTION_CODE_DUPLICATE"));
    }

    @Test
    @DisplayName("UC-10: Cấu hình Lịch học chi tiết thành công")
    void shouldAssignScheduleSlotsSuccessfully() throws Exception {
        CourseSection section = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS101.N01")
                .course(savedCourse)
                .semester(savedSemester)
                .lecturer(savedLecturer)
                .capacity(50)
                .build());

        ScheduleSlotRequest slotReq = ScheduleSlotRequest.builder()
                .roomId(savedRoomLarge.getId()) // Sức chứa 60 >= 50
                .dayOfWeek(DayOfWeek.MONDAY)
                .startPeriod(1)
                .endPeriod(3)
                .build();

        mockMvc.perform(post("/api/v1/academic/sections/" + section.getId() + "/schedules")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(slotReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scheduleSlots[0].roomNumber").value("B201"))
                .andExpect(jsonPath("$.data.scheduleSlots[0].dayOfWeek").value("MONDAY"));
    }

    @Test
    @DisplayName("UC-10: Chặn xếp lịch khi sức chứa phòng nhỏ hơn chỉ tiêu lớp (Phòng 30 < Lớp 50)")
    void shouldRejectWhenRoomCapacityInsufficient() throws Exception {
        CourseSection section = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS101.N01")
                .course(savedCourse)
                .semester(savedSemester)
                .capacity(50) // Lớp 50
                .build());

        ScheduleSlotRequest slotReq = ScheduleSlotRequest.builder()
                .roomId(savedRoomSmall.getId()) // Phòng chỉ có 30 chỗ
                .dayOfWeek(DayOfWeek.MONDAY)
                .startPeriod(1)
                .endPeriod(3)
                .build();

        mockMvc.perform(post("/api/v1/academic/sections/" + section.getId() + "/schedules")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(slotReq))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_ROOM_CAPACITY_INSUFFICIENT"));
    }

    @Test
    @DisplayName("UC-10: Chặn trùng lịch phòng học giữa 2 lớp học phần (BR-03)")
    void shouldRejectRoomCollision() throws Exception {
        // Lớp 1 đã học tại B201 vào Thứ 2 (Tiết 1-3)
        CourseSection section1 = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS101.N01")
                .course(savedCourse)
                .semester(savedSemester)
                .capacity(50)
                .build());

        scheduleSlotRepository.save(ScheduleSlot.builder()
                .section(section1)
                .room(savedRoomLarge)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startPeriod(1)
                .endPeriod(3)
                .build());

        // Lớp 2 cố xếp vào B201 vào Thứ 2 (Tiết 2-4) -> Trùng tiết 2-3
        CourseSection section2 = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS101.N02")
                .course(savedCourse)
                .semester(savedSemester)
                .capacity(50)
                .build());

        ScheduleSlotRequest slotReq = ScheduleSlotRequest.builder()
                .roomId(savedRoomLarge.getId())
                .dayOfWeek(DayOfWeek.MONDAY)
                .startPeriod(2)
                .endPeriod(4)
                .build();

        mockMvc.perform(post("/api/v1/academic/sections/" + section2.getId() + "/schedules")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(slotReq))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_ROOM_SCHEDULE_COLLISION"));
    }

    @Test
    @DisplayName("UC-11: Điều chỉnh sức chứa lớp học phần thành công")
    void shouldUpdateSectionCapacitySuccessfully() throws Exception {
        CourseSection section = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS101.N01")
                .course(savedCourse)
                .semester(savedSemester)
                .capacity(50)
                .enrolledCount(10)
                .status(SectionStatus.OPEN)
                .build());

        UpdateCapacityRequest request = UpdateCapacityRequest.builder()
                .newCapacity(55)
                .build();

        mockMvc.perform(put("/api/v1/academic/sections/" + section.getId() + "/capacity")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.capacity").value(55))
                .andExpect(jsonPath("$.data.availableSlots").value(45));
    }

    @Test
    @DisplayName("UC-11: Chặn hạ sức chứa thấp hơn số sinh viên đã đăng ký (BR-02 & BR-09)")
    void shouldRejectCapacityLowerThanEnrolledCount() throws Exception {
        CourseSection section = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS101.N01")
                .course(savedCourse)
                .semester(savedSemester)
                .capacity(50)
                .enrolledCount(25) // Hiện đã có 25 sinh viên
                .build());

        UpdateCapacityRequest request = UpdateCapacityRequest.builder()
                .newCapacity(20) // Cố hạ xuống 20 < 25
                .build();

        mockMvc.perform(put("/api/v1/academic/sections/" + section.getId() + "/capacity")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_CAPACITY_LOWER_THAN_ENROLLED"));
    }
}
