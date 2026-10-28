package com.unienroll.modules.engine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.StudentProfile;
import com.unienroll.modules.auth.entity.User;
import com.unienroll.modules.auth.entity.UserStatus;
import com.unienroll.modules.auth.repository.StudentProfileRepository;
import com.unienroll.modules.auth.repository.UserRepository;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.entity.Department;
import com.unienroll.modules.curriculum.entity.PrerequisiteCondition;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.curriculum.repository.DepartmentRepository;
import com.unienroll.modules.curriculum.repository.PrerequisiteConditionRepository;
import com.unienroll.modules.engine.dto.EnrollmentRequest;
import com.unienroll.modules.engine.entity.EnrollmentRecord;
import com.unienroll.modules.engine.entity.EnrollmentStatus;
import com.unienroll.modules.engine.entity.RegistrationPhase;
import com.unienroll.modules.engine.repository.EnrollmentRecordRepository;
import com.unienroll.modules.engine.repository.RegistrationPhaseRepository;
import com.unienroll.modules.reporting.entity.GradeRecord;
import com.unienroll.modules.reporting.entity.GradeStatus;
import com.unienroll.modules.reporting.repository.GradeRecordRepository;
import com.unienroll.modules.reporting.repository.RegistrationAuditLogRepository;
import com.unienroll.modules.scheduling.entity.CourseSection;
import com.unienroll.modules.scheduling.entity.Room;
import com.unienroll.modules.scheduling.entity.ScheduleSlot;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationPipelineTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EnrollmentRecordRepository enrollmentRepository;

    @Autowired
    private RegistrationPhaseRepository phaseRepository;

    @Autowired
    private CourseSectionRepository sectionRepository;

    @Autowired
    private ScheduleSlotRepository slotRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PrerequisiteConditionRepository prerequisiteRepository;

    @Autowired
    private GradeRecordRepository gradeRecordRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private StudentProfileRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationAuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String studentToken;
    private StudentProfile studentProfile;
    private Semester semester;
    private Department department;
    private Room room;
    private Course course101;
    private Course course102;
    private Course course999;
    private CourseSection section101;
    private CourseSection section102;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        enrollmentRepository.deleteAll();
        phaseRepository.deleteAll();
        slotRepository.deleteAll();
        sectionRepository.deleteAll();
        gradeRecordRepository.deleteAll();
        prerequisiteRepository.deleteAll();
        courseRepository.deleteAll();
        departmentRepository.deleteAll();
        roomRepository.deleteAll();
        semesterRepository.deleteAll();
        studentRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Tạo Sinh viên
        User user = User.builder()
                .username("student_pipeline")
                .password(passwordEncoder.encode("Password@123"))
                .email("student_pipeline@unienroll.edu.vn")
                .role(Role.ROLE_STUDENT)
                .status(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        studentProfile = studentRepository.save(StudentProfile.builder()
                .studentCode("SV2026_PIPE")
                .fullName("Lê Văn Kiểm Thử")
                .cohort("K65")
                .major("Kỹ thuật Phần mềm")
                .user(savedUser)
                .build());

        studentToken = jwtTokenProvider.generateAccessToken(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());

        // 2. Tạo Học kỳ
        semester = semesterRepository.save(Semester.builder()
                .code("2026_1")
                .name("Học kỳ 1 năm học 2026-2027")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2027, 1, 15))
                .status(SemesterStatus.ACTIVE)
                .build());

        // 3. Tạo Đợt đăng ký hợp lệ (đang mở)
        phaseRepository.save(RegistrationPhase.builder()
                .semester(semester)
                .name("Đợt 1 - Đăng ký chính thức")
                .startTime(Instant.now().minus(1, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(2, ChronoUnit.DAYS))
                .targetCohort("K65")
                .build());

        // 4. Tạo Phòng học
        room = roomRepository.save(Room.builder()
                .building("Tòa A")
                .roomNumber("A101")
                .capacity(50)
                .build());

        // 5. Tạo Khoa & Môn học
        department = departmentRepository.save(Department.builder()
                .code("IT")
                .name("Khoa CNTT")
                .build());

        course101 = courseRepository.save(Course.builder()
                .code("CS101")
                .name("Lập trình Cơ sở")
                .theoryCredits(3)
                .practicalCredits(0)
                .totalCredits(3)
                .department(department)
                .build());

        course102 = courseRepository.save(Course.builder()
                .code("CS102")
                .name("Cấu trúc Dữ liệu")
                .theoryCredits(4)
                .practicalCredits(0)
                .totalCredits(4)
                .department(department)
                .build());

        course999 = courseRepository.save(Course.builder()
                .code("CS999")
                .name("Môn Rất Nặng")
                .theoryCredits(25)
                .practicalCredits(0)
                .totalCredits(25)
                .department(department)
                .build());

        // 6. Tạo Lớp học phần
        section101 = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS101.N01")
                .course(course101)
                .semester(semester)
                .capacity(30)
                .enrolledCount(0)
                .build());

        slotRepository.save(ScheduleSlot.builder()
                .section(section101)
                .room(room)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startPeriod(1)
                .endPeriod(3)
                .build());

        section102 = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS102.N01")
                .course(course102)
                .semester(semester)
                .capacity(30)
                .enrolledCount(0)
                .build());
    }

    @Test
    @DisplayName("UC-14: Đăng ký thành công khi thỏa mãn toàn bộ 6 Invariants")
    void shouldEnrollSuccessfully() throws Exception {
        EnrollmentRequest request = EnrollmentRequest.builder()
                .sectionId(section101.getId())
                .build();

        mockMvc.perform(post("/api/v1/student/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sectionCode").value("CS101.N01"))
                .andExpect(jsonPath("$.data.status").value("ENROLLED"));

        // Kiểm tra số lượng đã đăng ký tăng lên 1
        CourseSection updated = sectionRepository.findById(section101.getId()).orElseThrow();
        assertEquals(1, updated.getEnrolledCount());
    }

    @Test
    @DisplayName("BR-01: Chặn đăng ký khi ngoài khung giờ mở cổng trả về HTTP 400 ERR_REGISTRATION_WINDOW_CLOSED")
    void shouldRejectWhenRegistrationWindowClosed() throws Exception {
        // Xóa các phase đang mở và tạo 1 phase đã hết hạn trong quá khứ
        phaseRepository.deleteAll();
        phaseRepository.save(RegistrationPhase.builder()
                .semester(semester)
                .name("Đợt đã đóng")
                .startTime(Instant.now().minus(5, ChronoUnit.DAYS))
                .endTime(Instant.now().minus(1, ChronoUnit.DAYS))
                .build());

        EnrollmentRequest request = EnrollmentRequest.builder()
                .sectionId(section101.getId())
                .build();

        mockMvc.perform(post("/api/v1/student/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_REGISTRATION_WINDOW_CLOSED"));
    }

    @Test
    @DisplayName("BR-04: Chặn đăng ký khi chưa đạt môn học tiên quyết trả về HTTP 400 ERR_PREREQUISITE_NOT_FULFILLED")
    void shouldRejectWhenPrerequisiteNotFulfilled() throws Exception {
        // Gán CS102 yêu cầu tiên quyết là CS101
        prerequisiteRepository.save(PrerequisiteCondition.builder()
                .targetCourse(course102)
                .prerequisiteCourse(course101)
                .build());

        // Sinh viên chưa học môn CS101 -> Thử đăng ký CS102
        EnrollmentRequest request = EnrollmentRequest.builder()
                .sectionId(section102.getId())
                .build();

        mockMvc.perform(post("/api/v1/student/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_PREREQUISITE_NOT_FULFILLED"));
    }

    @Test
    @DisplayName("BR-06: Chặn đăng ký 2 lớp của cùng một môn học trong học kỳ trả về HTTP 409 ERR_DUPLICATE_COURSE_REGISTRATION")
    void shouldRejectDuplicateCourseRegistration() throws Exception {
        // Lớp thứ 2 của cùng môn CS101
        CourseSection section101B = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS101.N02")
                .course(course101)
                .semester(semester)
                .capacity(30)
                .enrolledCount(0)
                .build());

        // Đăng ký lớp 1 trước
        mockMvc.perform(post("/api/v1/student/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(section101.getId()))))
                .andExpect(status().isOk());

        // Cố đăng ký tiếp lớp 2 của cùng môn CS101
        mockMvc.perform(post("/api/v1/student/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(section101B.getId()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_DUPLICATE_COURSE_REGISTRATION"));
    }

    @Test
    @DisplayName("BR-03: Chặn đăng ký trùng lịch học với môn đã chọn trả về HTTP 409 ERR_TIMETABLE_COLLISION")
    void shouldRejectTimetableCollision() throws Exception {
        // Section 101 học Thứ 2 (Tiết 1-3)
        // Section 102 xếp học Thứ 2 (Tiết 2-4) -> Trùng tiết 2-3
        slotRepository.save(ScheduleSlot.builder()
                .section(section102)
                .room(room)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startPeriod(2)
                .endPeriod(4)
                .build());

        // Đăng ký section 101 trước
        mockMvc.perform(post("/api/v1/student/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(section101.getId()))))
                .andExpect(status().isOk());

        // Cố đăng ký section 102 bị trùng thời khóa biểu
        mockMvc.perform(post("/api/v1/student/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(section102.getId()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_TIMETABLE_COLLISION"));
    }

    @Test
    @DisplayName("BR-05: Chặn đăng ký vượt quá hạn mức 24 tín chỉ trả về HTTP 400 ERR_CREDIT_LIMIT_EXCEEDED")
    void shouldRejectCreditLimitExceeded() throws Exception {
        CourseSection heavySection = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS999.N01")
                .course(course999) // 25 tín chỉ > 24
                .semester(semester)
                .capacity(30)
                .enrolledCount(0)
                .build());

        mockMvc.perform(post("/api/v1/student/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(heavySection.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_CREDIT_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("UC-15: Hủy đăng ký học phần thành công và thu hồi 1 slot trống cho lớp")
    void shouldDropSectionSuccessfully() throws Exception {
        // Đăng ký sẵn 4 môn (tổng 15 tín chỉ) để sau khi hủy 1 môn (3 tín chỉ) vẫn còn 12 tín chỉ >= min 12
        Course c1 = courseRepository.save(Course.builder().code("M1").name("M1").totalCredits(4).department(department).build());
        Course c2 = courseRepository.save(Course.builder().code("M2").name("M2").totalCredits(4).department(department).build());
        Course c3 = courseRepository.save(Course.builder().code("M3").name("M3").totalCredits(4).department(department).build());

        CourseSection s1 = sectionRepository.save(CourseSection.builder().sectionCode("S1").course(c1).semester(semester).capacity(30).enrolledCount(0).build());
        CourseSection s2 = sectionRepository.save(CourseSection.builder().sectionCode("S2").course(c2).semester(semester).capacity(30).enrolledCount(0).build());
        CourseSection s3 = sectionRepository.save(CourseSection.builder().sectionCode("S3").course(c3).semester(semester).capacity(30).enrolledCount(0).build());

        enrollmentRepository.save(EnrollmentRecord.builder().student(studentProfile).section(s1).course(c1).semester(semester).status(EnrollmentStatus.ENROLLED).enrolledAt(Instant.now()).build());
        enrollmentRepository.save(EnrollmentRecord.builder().student(studentProfile).section(s2).course(c2).semester(semester).status(EnrollmentStatus.ENROLLED).enrolledAt(Instant.now()).build());
        enrollmentRepository.save(EnrollmentRecord.builder().student(studentProfile).section(s3).course(c3).semester(semester).status(EnrollmentStatus.ENROLLED).enrolledAt(Instant.now()).build());

        // Đăng ký section101 (3 tín chỉ) -> tổng 15 tín chỉ
        mockMvc.perform(post("/api/v1/student/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(section101.getId()))))
                .andExpect(status().isOk());

        assertEquals(1, sectionRepository.findById(section101.getId()).orElseThrow().getEnrolledCount());

        // Thực hiện HỦY môn section101 (còn 12 tín chỉ)
        mockMvc.perform(delete("/api/v1/student/registrations/" + section101.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DROPPED"));

        // Slot của section101 giảm về 0
        assertEquals(0, sectionRepository.findById(section101.getId()).orElseThrow().getEnrolledCount());
    }

    @Test
    @DisplayName("UC-15: Chặn hủy học phần nếu số tín chỉ còn lại < 12 trả về HTTP 400 ERR_MINIMUM_CREDIT_UNMET")
    void shouldRejectDropWhenResultingCreditsBelowMinimum() throws Exception {
        // Đăng ký môn section101 (3 tín chỉ)
        mockMvc.perform(post("/api/v1/student/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(section101.getId()))))
                .andExpect(status().isOk());

        // Hủy môn này thì số tín chỉ còn 0 < 12 -> Bị chặn
        mockMvc.perform(delete("/api/v1/student/registrations/" + section101.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_MINIMUM_CREDIT_UNMET"));
    }

    @Test
    @DisplayName("UC-16: Xem thời khóa biểu và danh sách lớp đã đăng ký thành công")
    void shouldGetActiveRegistrationsSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/student/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(section101.getId()))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/student/registrations/active?semesterId=" + semester.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCredits").value(3))
                .andExpect(jsonPath("$.data.enrolledSections", hasSize(1)))
                .andExpect(jsonPath("$.data.enrolledSections[0].sectionCode").value("CS101.N01"));
    }
}
