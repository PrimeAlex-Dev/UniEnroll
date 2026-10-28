package com.unienroll.modules.engine.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
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
import com.unienroll.modules.engine.entity.EnrollmentRecord;
import com.unienroll.modules.engine.entity.EnrollmentStatus;
import com.unienroll.modules.engine.entity.RegistrationPhase;
import com.unienroll.modules.engine.repository.EnrollmentRecordRepository;
import com.unienroll.modules.engine.repository.RegistrationPhaseRepository;
import com.unienroll.modules.reporting.repository.GradeRecordRepository;
import com.unienroll.modules.reporting.repository.RegistrationAuditLogRepository;
import com.unienroll.modules.scheduling.entity.CourseSection;
import com.unienroll.modules.scheduling.entity.SectionStatus;
import com.unienroll.modules.scheduling.entity.Semester;
import com.unienroll.modules.scheduling.entity.SemesterStatus;
import com.unienroll.modules.scheduling.repository.CourseSectionRepository;
import com.unienroll.modules.scheduling.repository.RoomRepository;
import com.unienroll.modules.scheduling.repository.ScheduleSlotRepository;
import com.unienroll.modules.scheduling.repository.SemesterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class RegistrationConcurrencyTest {

    @Autowired
    private RegistrationEngineService registrationEngineService;

    @Autowired
    private CourseSectionRepository sectionRepository;

    @Autowired
    private EnrollmentRecordRepository enrollmentRepository;

    @Autowired
    private RegistrationPhaseRepository phaseRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private StudentProfileRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleSlotRepository slotRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private GradeRecordRepository gradeRecordRepository;

    @Autowired
    private RegistrationAuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private CourseSection targetSection;
    private final List<StudentProfile> competitorStudents = new ArrayList<>();

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
        roomRepository.deleteAll();
        semesterRepository.deleteAll();
        studentRepository.deleteAll();
        userRepository.deleteAll();
        competitorStudents.clear();

        // 1. Tạo Học kỳ
        Semester semester = semesterRepository.save(Semester.builder()
                .code("2026_CONCURRENCY")
                .name("Học kỳ Kiểm thử Đồng thời")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2027, 1, 15))
                .status(SemesterStatus.ACTIVE)
                .build());

        // 2. Mở Đợt đăng ký hợp lệ
        phaseRepository.save(RegistrationPhase.builder()
                .semester(semester)
                .name("Đợt Đăng ký Tải cao")
                .startTime(Instant.now().minus(1, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(2, ChronoUnit.DAYS))
                .build());

        // 3. Tạo Khoa và Môn học (3 tín chỉ)
        Department dept = departmentRepository.save(Department.builder().code("IT").name("Khoa CNTT").build());
        Course course = courseRepository.save(Course.builder()
                .code("CS_CONC")
                .name("Kiểm thử Đồng thời")
                .theoryCredits(3)
                .practicalCredits(0)
                .totalCredits(3)
                .department(dept)
                .build());

        // 4. Tạo Lớp học phần: Sức chứa Cmax = 10, Hiện tại ĐÃ CÓ 9 sinh viên đăng ký trước
        // CHỈ CÒN ĐÚNG 1 SLOT TRỐNG DUY NHẤT!
        targetSection = sectionRepository.save(CourseSection.builder()
                .sectionCode("CS_CONC.N01")
                .course(course)
                .semester(semester)
                .capacity(10)
                .enrolledCount(9) // Còn đúng 1 slot!
                .status(SectionStatus.OPEN)
                .build());

        // Tạo sẵn 9 sinh viên đã chiếm 9 slot trước đó
        for (int i = 1; i <= 9; i++) {
            User earlyUser = userRepository.save(User.builder()
                    .username("early_student_" + i)
                    .password(passwordEncoder.encode("Pass@123"))
                    .email("early" + i + "@unienroll.edu.vn")
                    .role(Role.ROLE_STUDENT)
                    .status(UserStatus.ACTIVE)
                    .build());

            StudentProfile earlyProfile = studentRepository.save(StudentProfile.builder()
                    .studentCode("EARLY_" + i)
                    .fullName("Sinh viên Đã có chỗ " + i)
                    .cohort("K65")
                    .user(earlyUser)
                    .build());

            enrollmentRepository.save(EnrollmentRecord.builder()
                    .student(earlyProfile)
                    .section(targetSection)
                    .course(course)
                    .semester(semester)
                    .status(EnrollmentStatus.ENROLLED)
                    .enrolledAt(Instant.now().minus(1, ChronoUnit.HOURS))
                    .build());
        }

        // 5. Chuẩn bị 50 sinh viên mới (Competitors) cùng tranh giành 1 slot cuối cùng
        for (int i = 1; i <= 50; i++) {
            User competitorUser = userRepository.save(User.builder()
                    .username("competitor_" + i)
                    .password(passwordEncoder.encode("Pass@123"))
                    .email("competitor" + i + "@unienroll.edu.vn")
                    .role(Role.ROLE_STUDENT)
                    .status(UserStatus.ACTIVE)
                    .build());

            StudentProfile competitorProfile = studentRepository.save(StudentProfile.builder()
                    .studentCode("COMP_" + i)
                    .fullName("Thí sinh Tranh slot " + i)
                    .cohort("K65")
                    .user(competitorUser)
                    .build());

            competitorStudents.add(competitorProfile);
        }
    }

    @Test
    @DisplayName("MULTI-THREADED CONCURRENCY TEST: 50 luồng đồng thời tranh giành 1 slot cuối cùng -> Triệt tiêu 100% Race Condition (Zero Overbooking)")
    void shouldGuaranteeZeroOverbookingUnderHighConcurrency() throws InterruptedException {
        int numberOfThreads = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger capacityFullCount = new AtomicInteger(0);
        List<Exception> unexpectedExceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numberOfThreads; i++) {
            final StudentProfile student = competitorStudents.get(i);
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    // Chờ tiếng súng lệnh xuất phát để cả 50 luồng bắn request cùng 1 mili-giây
                    startGun.await();
                    registrationEngineService.enroll(student.getId(), targetSection.getId(), "127.0.0.1");
                    successCount.incrementAndGet();
                } catch (BusinessException ex) {
                    if (ex.getErrorCode() == ErrorCode.ERR_SECTION_CAPACITY_FULL) {
                        capacityFullCount.incrementAndGet();
                    } else {
                        unexpectedExceptions.add(ex);
                    }
                } catch (Exception ex) {
                    unexpectedExceptions.add(ex);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Chờ toàn bộ 50 thread đã sẵn sàng ở vạch xuất phát
        readyLatch.await();
        // BẮN SÚNG LỆNH! Cả 50 thread cùng ập vào thực thi enroll() đồng thời
        startGun.countDown();

        // Chờ toàn bộ 50 thread hoàn tất
        doneLatch.await();
        executorService.shutdown();

        // ==========================================
        // NGHIỆM THU RÀNG BUỘC TOÀN VẸN (INVARIANTS):
        // ==========================================
        // 1. Chỉ duy nhất 1 sinh viên giành được 1 slot trống cuối cùng
        assertEquals(1, successCount.get(), "Số lượng đăng ký thành công phải đúng bằng 1");

        // 2. Đúng 49 sinh viên còn lại phải nhận mã lỗi ERR_SECTION_CAPACITY_FULL
        assertEquals(49, capacityFullCount.get(), "Đúng 49 yêu cầu phải bị từ chối do hết chỗ");

        // 3. Không có bất kỳ lỗi không mong muốn nào phát sinh
        assertEquals(0, unexpectedExceptions.size(), "Không được có ngoại lệ không mong muốn: " + unexpectedExceptions);

        // 4. Kiểm tra trực tiếp Cơ sở dữ liệu:
        CourseSection finalSection = sectionRepository.findById(targetSection.getId()).orElseThrow();

        // Nenrolled chốt đúng bằng Cmax (10/10), TUYỆT ĐỐI KHÔNG BỊ OVERBOOKING (11, 12...)
        assertEquals(10, finalSection.getEnrolledCount(), "Sĩ số lớp phải chốt đúng bằng sức chứa tối đa (10)");
        assertEquals(SectionStatus.CLOSED, finalSection.getStatus(), "Trạng thái lớp phải chuyển sang CLOSED");

        // Tổng số bản ghi ENROLLED trong database cho lớp này là 10 (9 ban đầu + đúng 1 người vừa đăng ký)
        List<EnrollmentRecord> enrolledRecords = enrollmentRepository.findBySectionIdAndStatus(
                targetSection.getId(), EnrollmentStatus.ENROLLED);
        assertEquals(10, enrolledRecords.size(), "Tổng số bản ghi ENROLLED thực tế trong DB phải đúng bằng 10");
    }
}
